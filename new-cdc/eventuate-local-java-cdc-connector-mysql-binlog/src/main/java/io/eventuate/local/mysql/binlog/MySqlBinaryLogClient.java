package io.eventuate.local.mysql.binlog;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.NullEventDataDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.UpdateRowsEventDataDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.WriteRowsEventDataDeserializer;
import com.google.common.collect.ImmutableSet;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogEntryHandler;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.SchemaAndTable;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetKafkaStore;
import io.eventuate.local.db.log.common.OffsetStore;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;

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

  private static final SchemaAndTable MONITORING_SCHEMA_AND_TABLE =
          new SchemaAndTable(new EventuateSchema().getEventuateDatabaseSchema(), "cdc_monitoring");

  private String name;
  private BinaryLogClient client;
  private long binlogClientUniqueId;
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

  public MySqlBinaryLogClient(MeterRegistry meterRegistry,
                              String dbUserName,
                              String dbPassword,
                              String dataSourceUrl,
                              DataSource dataSource,
                              long binlogClientUniqueId,
                              String clientName,
                              int connectionTimeoutInMilliseconds,
                              int maxAttemptsForBinlogConnection,
                              CuratorFramework curatorFramework,
                              String leadershipLockPath,
                              OffsetStore offsetStore,
                              Optional<DebeziumBinlogOffsetKafkaStore> debeziumBinlogOffsetKafkaStore,
                              long replicationLagMeasuringIntervalInMilliseconds,
                              int monitoringRetryIntervalInMilliseconds,
                              int monitoringRetryAttempts) {

    super(meterRegistry,
            dbUserName,
            dbPassword,
            dataSourceUrl,
            curatorFramework,
            leadershipLockPath,
            dataSource,
            binlogClientUniqueId,
            replicationLagMeasuringIntervalInMilliseconds,
            monitoringRetryIntervalInMilliseconds,
            monitoringRetryAttempts);

    this.binlogClientUniqueId = binlogClientUniqueId;
    this.extractor = new MySqlBinlogEntryExtractor(dataSource);
    this.timestampExtractor = new MySqlBinlogCdcMonitoringTimestampExtractor(dataSource);
    this.name = clientName;
    this.connectionTimeoutInMilliseconds = connectionTimeoutInMilliseconds;
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
    this.offsetStore = offsetStore;
    this.debeziumBinlogOffsetKafkaStore = debeziumBinlogOffsetKafkaStore;
  }

  public String getName() {
    return name;
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
  protected void leaderStart() {
    super.leaderStart();

    logger.info("mysql binlog client started");

    stopCountDownLatch = new CountDownLatch(1);
    running.set(true);

    client = new BinaryLogClient(host, port, dbUserName, dbPassword);
    client.setServerId(binlogClientUniqueId);
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

          cdcMonitoringTableId = Optional.empty();
          tableMapEventByTableId.clear();

          SchemaAndTable schemaAndTable = new SchemaAndTable(tableMapEvent.getDatabase(), tableMapEvent.getTable());

          if (schemaAndTable.equals(MONITORING_SCHEMA_AND_TABLE)) {
            cdcMonitoringTableId = Optional.of(tableMapEvent.getTableId());
            tableMapEventByTableId.put(tableMapEvent.getTableId(), tableMapEvent);
            break;
          }

          boolean shouldHandleTable = binlogEntryHandlers
                  .stream()
                  .map(BinlogEntryHandler::getSchemaAndTable)
                  .anyMatch(schemaAndTable::equals);

          if (shouldHandleTable) {
            tableMapEventByTableId.put(tableMapEvent.getTableId(), tableMapEvent);
          }

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
    });

    connectWithRetriesOnFail();

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
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
    offset = ((EventHeaderV4) event.getHeader()).getPosition();
    WriteRowsEventData eventData = event.getData();

    BinlogFileOffset binlogFileOffset = new BinlogFileOffset(binlogFilename, offset);
    logger.info("mysql binlog client got event with offset {}", binlogFileOffset);

    if (cdcMonitoringTableId.map(id -> id.equals(eventData.getTableId())).orElse(false)) {
      onMonitoringEventReceived(timestampExtractor.extract(MONITORING_SCHEMA_AND_TABLE, eventData));
    }
    else if (tableMapEventByTableId.containsKey(eventData.getTableId())) {
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

    offsetStore.save(binlogFileOffset);
  }

  private void handleUpdateRowsEvent(Event event) {
    UpdateRowsEventData eventData = event.getData();

    if (eventData == null) {
      return;
    }

    if (cdcMonitoringTableId.map(id -> id.equals(eventData.getTableId())).orElse(false)) {
      onMonitoringEventReceived(timestampExtractor.extract(MONITORING_SCHEMA_AND_TABLE, eventData));
    }

    offset = ((EventHeaderV4) event.getHeader()).getPosition();
    BinlogFileOffset binlogFileOffset = new BinlogFileOffset(binlogFilename, offset);
    offsetStore.save(binlogFileOffset);
  }

  private void connectWithRetriesOnFail() {
    for (int i = 1;; i++) {
      try {
        logger.info("trying to connect to mysql binlog");
        client.connect(connectionTimeoutInMilliseconds);
        logger.info("connection to mysql binlog succeed");
        break;
      } catch (TimeoutException | IOException e) {
        logger.error("connection to mysql binlog failed");
        if (i == maxAttemptsForBinlogConnection) {
          logger.error("connection attempts exceeded");
          throw new RuntimeException(e);
        }
        try {
          Thread.sleep(connectionTimeoutInMilliseconds);
        } catch (InterruptedException ex) {
          running.set(false);
          stopCountDownLatch.countDown();
          return;
        }
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
            new WriteRowsEventDataDeserializer(
                    tableMapEventByTableId));

    eventDeserializer.setEventDataDeserializer(EventType.EXT_WRITE_ROWS,
            new WriteRowsEventDataDeserializer(
                    tableMapEventByTableId).setMayContainExtraInformation(true));

    eventDeserializer.setEventDataDeserializer(EventType.UPDATE_ROWS,
            new UpdateRowsEventDataDeserializer(
                    tableMapEventByTableId));

    eventDeserializer.setEventDataDeserializer(EventType.EXT_UPDATE_ROWS,
            new UpdateRowsEventDataDeserializer(
                    tableMapEventByTableId).setMayContainExtraInformation(true));

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

    stopCountDownLatch.countDown();
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
