package io.eventuate.local.mysql.binlog;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.NullEventDataDeserializer;
import com.google.common.collect.ImmutableSet;
import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetKafkaStore;
import io.eventuate.local.db.log.common.OffsetStore;
import io.micrometer.core.instrument.MeterRegistry;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class MySqlBinaryLogClient extends DbLogClient {

  private static final Set<EventType> SUPPORTED_EVENTS = ImmutableSet.of(EventType.TABLE_MAP,
          EventType.ROTATE,
          EventType.WRITE_ROWS,
          EventType.EXT_WRITE_ROWS,
          EventType.UPDATE_ROWS,
          EventType.EXT_UPDATE_ROWS);

  private Long uniqueId;
  private BinaryLogClient client;
  private final Map<Long, TableMapEventData> tableMapEventByTableId = new HashMap<>();
  private String binlogFilename;
  private long offset;
  private MySqlBinlogEntryExtractor extractor;
  private MySqlBinlogCdcMonitoringTimestampExtractor timestampExtractor;
  private int connectionTimeoutInMilliseconds;
  private int maxAttemptsForBinlogConnection;
  private Optional<DebeziumBinlogOffsetKafkaStore> debeziumBinlogOffsetKafkaStore;
  private int rowsToSkip;
  private OffsetStore offsetStore;

  private Optional<Long> cdcMonitoringTableId = Optional.empty();
  private MySqlCdcProcessingStatusService mySqlCdcProcessingStatusService;

  public MySqlBinaryLogClient(MeterRegistry meterRegistry,
                              String dbUserName,
                              String dbPassword,
                              String dataSourceUrl,
                              DataSource dataSource,
                              String readerName,
                              Long uniqueId,
                              int connectionTimeoutInMilliseconds,
                              int maxAttemptsForBinlogConnection,
                              String leaderLockId,
                              LeaderSelectorFactory leaderSelectorFactory,
                              OffsetStore offsetStore,
                              Optional<DebeziumBinlogOffsetKafkaStore> debeziumBinlogOffsetKafkaStore,
                              long replicationLagMeasuringIntervalInMilliseconds,
                              int monitoringRetryIntervalInMilliseconds,
                              int monitoringRetryAttempts) {

    super(meterRegistry,
            dbUserName,
            dbPassword,
            dataSourceUrl,
            leaderLockId,
            leaderSelectorFactory,
            dataSource,
            readerName,
            replicationLagMeasuringIntervalInMilliseconds,
            monitoringRetryIntervalInMilliseconds,
            monitoringRetryAttempts);

    this.extractor = new MySqlBinlogEntryExtractor(dataSource);
    this.timestampExtractor = new MySqlBinlogCdcMonitoringTimestampExtractor(dataSource);
    this.uniqueId = uniqueId;
    this.connectionTimeoutInMilliseconds = connectionTimeoutInMilliseconds;
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
    this.offsetStore = offsetStore;
    this.debeziumBinlogOffsetKafkaStore = debeziumBinlogOffsetKafkaStore;

    mySqlCdcProcessingStatusService = new MySqlCdcProcessingStatusService(dataSourceUrl, dbUserName, dbPassword);
  }

  public Optional<MigrationInfo> getMigrationInfo() {
    if (offsetStore.getLastBinlogFileOffset().isPresent()) {
      return Optional.empty();
    }

    return debeziumBinlogOffsetKafkaStore
            .flatMap(OffsetKafkaStore::getLastBinlogFileOffset)
            .map(MigrationInfo::new);
  }

  @Override
  public CdcProcessingStatusService getCdcProcessingStatusService() {
    return mySqlCdcProcessingStatusService;
  }

  @Override
  protected void leaderStart() {
    super.leaderStart();

    logger.info("mysql binlog client started");

    stopCountDownLatch = new CountDownLatch(1);
    running.set(true);

    client = new BinaryLogClient(host, port, dbUserName, dbPassword);
    client.setServerId(uniqueId);
    client.setKeepAliveInterval(5 * 1000);

    Optional<BinlogFileOffset> binlogFileOffset = getStartingBinlogFileOffset();

    BinlogFileOffset bfo = binlogFileOffset.orElse(new BinlogFileOffset("", 4L));
    rowsToSkip = bfo.getRowsToSkip();

    logger.info("mysql binlog starting offset {}", bfo);
    client.setBinlogFilename(bfo.getBinlogFilename());
    client.setBinlogPosition(bfo.getOffset());

    client.setEventDeserializer(getEventDeserializer());
    client.registerEventListener(event -> {
      switch (event.getHeader().getEventType()) {
        case TABLE_MAP: {
          TableMapEventData tableMapEvent = event.getData();

          if (cdcMonitoringDao.isMonitoringTableChange(tableMapEvent.getDatabase(), tableMapEvent.getTable())) {
            cdcMonitoringTableId = Optional.of(tableMapEvent.getTableId());
            tableMapEventByTableId.put(tableMapEvent.getTableId(), tableMapEvent);
            break;
          }

          SchemaAndTable schemaAndTable = new SchemaAndTable(tableMapEvent.getDatabase(), tableMapEvent.getTable());

          boolean shouldHandleTable = binlogEntryHandlers
                  .stream()
                  .map(BinlogEntryHandler::getSchemaAndTable)
                  .anyMatch(schemaAndTable::equals);

          if (shouldHandleTable) {
            tableMapEventByTableId.put(tableMapEvent.getTableId(), tableMapEvent);
          } else {
            tableMapEventByTableId.remove(tableMapEvent.getTableId());
          }

          dbLogMetrics.onBinlogEntryProcessed();

          break;
        }
        case EXT_WRITE_ROWS: {
          handleWriteRowsEvent(event, binlogFileOffset);
          break;
        }
        case WRITE_ROWS: {
          handleWriteRowsEvent(event, binlogFileOffset);
          break;
        }
        case EXT_UPDATE_ROWS: {
          handleUpdateRowsEvent(event);
          break;
        }
        case UPDATE_ROWS: {
          handleUpdateRowsEvent(event);
          break;
        }
        case ROTATE: {
          RotateEventData eventData = event.getData();
          if (eventData != null) {
            binlogFilename = eventData.getBinlogFilename();
          }
          break;
        }
      }

      saveEndingOffsetOfLastProcessedEvent(event);
    });

    connectWithRetriesOnFail();

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      handleProcessingFailException(e);
    }
  }

  private Optional<BinlogFileOffset> getStartingBinlogFileOffset() {
    Optional<BinlogFileOffset> binlogFileOffset = offsetStore.getLastBinlogFileOffset();

    logger.info("mysql binlog client received offset from the offset store: {}", binlogFileOffset);

    if (!binlogFileOffset.isPresent()) {
      logger.info("mysql binlog client received empty offset from the offset store, retrieving debezium offset");
      binlogFileOffset = debeziumBinlogOffsetKafkaStore.flatMap(OffsetKafkaStore::getLastBinlogFileOffset);
      logger.info("mysql binlog client received offset from the debezium offset store: {}", binlogFileOffset);
    }

    return binlogFileOffset;
  }

  private void handleWriteRowsEvent(Event event, Optional<BinlogFileOffset> startingBinlogFileOffset) {
    if (rowsToSkip > 0) {
      rowsToSkip--;
      return;
    }

    logger.info("Got binlog event {}", event);
    WriteRowsEventData eventData = event.getData();

    offset = extractOffset(event);
    logger.info("mysql binlog client got event with offset {}/{}", binlogFilename, offset);

    if (isCdcMonitoringTableId(eventData.getTableId())) {
      onLagMeasurementEventReceived(eventData);
    } else if (tableMapEventByTableId.containsKey(eventData.getTableId())) {
      TableMapEventData tableMapEventData = tableMapEventByTableId.get(eventData.getTableId());

      SchemaAndTable schemaAndTable = new SchemaAndTable(tableMapEventData.getDatabase(), tableMapEventData.getTable());

      BinlogEntry entry = extractor.extract(schemaAndTable, eventData, binlogFilename, offset);


      if (!shouldSkipEntry(startingBinlogFileOffset, entry.getBinlogFileOffset())) {
        binlogEntryHandlers
              .stream()
              .filter(bh -> bh.isFor(schemaAndTable))
              .forEach(binlogEntryHandler -> binlogEntryHandler.publish(entry));
      }
    }

    onEventReceived();
    saveOffset(event);
  }

  private void onLagMeasurementEventReceived(WriteRowsEventData eventData) {
    dbLogMetrics.onLagMeasurementEventReceived(timestampExtractor.extract(cdcMonitoringDao.getMonitoringSchemaAndTable(), eventData));
  }

  private void handleUpdateRowsEvent(Event event) {
    UpdateRowsEventData eventData = event.getData();

    if (eventData == null) {
      return;
    }

    if (isCdcMonitoringTableId(eventData.getTableId())) {
      onLagMeasurementEventReceived(eventData);
    }

    onEventReceived();
    saveOffset(event);
  }

  private void onLagMeasurementEventReceived(UpdateRowsEventData eventData) {
    dbLogMetrics.onLagMeasurementEventReceived(timestampExtractor.extract(cdcMonitoringDao.getMonitoringSchemaAndTable(), eventData));
  }

  private long extractOffset(Event event) {
    return ((EventHeaderV4) event.getHeader()).getPosition();
  }

  private void saveOffset(Event event) {
    offset = extractOffset(event);
    BinlogFileOffset binlogFileOffset = new BinlogFileOffset(binlogFilename, offset);
    offsetStore.save(binlogFileOffset);
  }

  private boolean isCdcMonitoringTableId(Long id) {
    return cdcMonitoringTableId.map(id::equals).orElse(false);
  }

  private void connectWithRetriesOnFail() {
    for (int i = 1;; i++) {
      try {
        logger.info("trying to connect to mysql binlog");
        client.connect(connectionTimeoutInMilliseconds);
        onConnected();
        logger.info("connection to mysql binlog succeed");
        break;
      } catch (TimeoutException | IOException e) {
        onDisconnected();
        logger.error("connection to mysql binlog failed");
        if (i == maxAttemptsForBinlogConnection) {
          handleProcessingFailException(e);
        }
        try {
          Thread.sleep(connectionTimeoutInMilliseconds);
        } catch (InterruptedException ex) {
          handleProcessingFailException(ex);
        }
      }
      catch (Exception e) {
        handleProcessingFailException(e);
      }
    }
  }

  private EventDeserializer getEventDeserializer() {
    EventDeserializer eventDeserializer = new EventDeserializer();

    Arrays.stream(EventType.values()).forEach(eventType -> {
      if (!SUPPORTED_EVENTS.contains(eventType)) {
        eventDeserializer.setEventDataDeserializer(eventType,
                new NullEventDataDeserializer());
      }
    });

    eventDeserializer.setEventDataDeserializer(EventType.WRITE_ROWS,
            new WriteRowsDeserializer(tableMapEventByTableId, dbLogMetrics));

    eventDeserializer.setEventDataDeserializer(EventType.EXT_WRITE_ROWS,
            new WriteRowsDeserializer(tableMapEventByTableId, dbLogMetrics).setMayContainExtraInformation(true));

    eventDeserializer.setEventDataDeserializer(EventType.UPDATE_ROWS,
            new UpdateRowsDeserializer(tableMapEventByTableId, dbLogMetrics));

    eventDeserializer.setEventDataDeserializer(EventType.EXT_UPDATE_ROWS,
            new UpdateRowsDeserializer(tableMapEventByTableId, dbLogMetrics).setMayContainExtraInformation(true));

    return eventDeserializer;
  }

  @Override
  protected void leaderStop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }

    try {
      client.disconnect();
    } catch (IOException e) {
      logger.error("Cannot stop the MySqlBinaryLogClient", e);
    }

    stopMetrics();

    stopCountDownLatch.countDown();
  }

  private void saveEndingOffsetOfLastProcessedEvent(Event event) {
    long position = ((EventHeaderV4) event.getHeader()).getNextPosition();
    if (mySqlCdcProcessingStatusService != null) {
      mySqlCdcProcessingStatusService.saveEndingOffsetOfLastProcessedEvent(position);
    }
  }

  public static class MigrationInfo {
    private BinlogFileOffset binlogFileOffset;

    public MigrationInfo(BinlogFileOffset binlogFileOffset) {

      this.binlogFileOffset = binlogFileOffset;
    }

    public BinlogFileOffset getBinlogFileOffset() {
      return binlogFileOffset;
    }
  }
}
