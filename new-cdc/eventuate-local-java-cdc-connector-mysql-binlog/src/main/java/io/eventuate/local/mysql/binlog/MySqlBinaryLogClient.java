package io.eventuate.local.mysql.binlog;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.GtidEventDataDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.NullEventDataDeserializer;
import com.google.common.collect.ImmutableSet;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetKafkaStore;
import io.eventuate.local.common.OffsetStore;
import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplate;
import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplateFactory;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class MySqlBinaryLogClient extends DbLogClient {

  private static final Set<EventType> SUPPORTED_EVENTS = ImmutableSet.of(EventType.TABLE_MAP,
          EventType.ROTATE,
          EventType.GTID,
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
  private OffsetStoreCreator offsetStoreCreator;
  private OffsetStore offsetStore;
  private boolean useGTIDsWhenPossible;

  private Optional<Long> cdcMonitoringTableId = Optional.empty();
  private MySqlCdcProcessingStatusService mySqlCdcProcessingStatusService;

  private Optional<Gtid> lastGTID = Optional.empty();
  private DataSource rootDataSource;
  private CdcDataPublisherTransactionTemplateFactory cdcDataPublisherTransactionTemplateFactory;
  private CdcDataPublisherTransactionTemplate cdcDataPublisherTransactionTemplate;

  public MySqlBinaryLogClient(DataProducerFactory dataProducerFactory,
                              CdcDataPublisherFactory cdcDataPublisherFactory,
                              CdcDataPublisherTransactionTemplateFactory cdcDataPublisherTransactionTemplateFactory,
                              MeterRegistry meterRegistry,
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
                              OffsetStoreCreator offsetStoreCreator,
                              Optional<DebeziumBinlogOffsetKafkaStore> debeziumBinlogOffsetKafkaStore,
                              long replicationLagMeasuringIntervalInMilliseconds,
                              int monitoringRetryIntervalInMilliseconds,
                              int monitoringRetryAttempts,
                              boolean useGTIDsWhenPossible) {

    super(dataProducerFactory,
            cdcDataPublisherFactory,
            meterRegistry,
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
    this.offsetStoreCreator = offsetStoreCreator;
    this.debeziumBinlogOffsetKafkaStore = debeziumBinlogOffsetKafkaStore;
    this.useGTIDsWhenPossible = useGTIDsWhenPossible;
    this.cdcDataPublisherTransactionTemplateFactory = cdcDataPublisherTransactionTemplateFactory;

    rootDataSource = createRootDataSource(dataSourceUrl, dbUserName, dbPassword);
    mySqlCdcProcessingStatusService = new MySqlCdcProcessingStatusService(rootDataSource);
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
  public CdcProcessingStatusService getCdcProcessingStatusService() {
    return mySqlCdcProcessingStatusService;
  }

  @Override
  protected void leaderStart() {
    super.leaderStart();
    logger.info("mysql binlog client started");

    offsetStore = offsetStoreCreator.create(dataProducer);
    cdcDataPublisherTransactionTemplate = cdcDataPublisherTransactionTemplateFactory.create(dataProducer);

    stopCountDownLatch = new CountDownLatch(1);
    running.set(true);

    createClientAndConnect();

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void createClientAndConnect() {
    client = new BinaryLogClient(host, port, dbUserName, dbPassword);
    client.setServerId(binlogClientUniqueId);
    client.setKeepAliveInterval(5 * 1000);

    Optional<BinlogFileOffset> binlogFileOffset = getStartingBinlogFileOffset();

    BinlogFileOffset bfo = binlogFileOffset.orElse(new BinlogFileOffset("", 4L));
    rowsToSkip = bfo.getRowsToSkip();

    logger.info("mysql binlog starting offset {}", bfo);

    if (shouldStartWithGtid(bfo)) {
      logger.info(String.format("GTID exists: %s, used as starting point", bfo.getGtid().get()));
      client.setGtidSet(bfo.getGtid().get().getValue());
    } else {
      logger.info("GTID does not exist starting using offset and binlog file name");
      client.setBinlogPosition(bfo.getOffset());
      client.setBinlogFilename(bfo.getBinlogFilename());
    }

    client.setEventDeserializer(getEventDeserializer());
    client.registerEventListener(event -> handleEvent(event, binlogFileOffset));

    connectWithRetriesOnFail();
  }

  private boolean shouldStartWithGtid(BinlogFileOffset binlogFileOffset) {
    return useGTIDsWhenPossible && isGTIDSupported() && binlogFileOffset.getGtid().isPresent();
  }


  private void handleEvent(Event event, Optional<BinlogFileOffset> binlogFileOffset) {
    switch (event.getHeader().getEventType()) {
      case TABLE_MAP: {
        TableMapEventData tableMapEvent = event.getData();

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
      case GTID: {
        if (!useGTIDsWhenPossible) {
          break;
        }

        GtidEventData gtidEventData = event.getData();
        Gtid gtid = new Gtid(gtidEventData.getGtid());
        lastGTID = Optional.of(gtid);
        logger.info(String.format("received GTID %s", gtid));
        break;
      }
    }

    saveEndingOffsetOfLastProcessedEvent(event);
  }

  private boolean isGTIDSupported() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(rootDataSource);

    SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("SHOW GLOBAL VARIABLES LIKE 'GTID_MODE'");

    if (sqlRowSet.next()) {
      boolean supported = !"OFF".equalsIgnoreCase(sqlRowSet.getString(2));
      logger.info("GTID mode support = {}", supported);
      return supported;
    }

    return false;
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
      dbLogMetrics.onLagMeasurementEventReceived(timestampExtractor.extract(MONITORING_SCHEMA_AND_TABLE, eventData));
    }
    else if (tableMapEventByTableId.containsKey(eventData.getTableId())) {
      TableMapEventData tableMapEventData = tableMapEventByTableId.get(eventData.getTableId());

      SchemaAndTable schemaAndTable = new SchemaAndTable(tableMapEventData.getDatabase(), tableMapEventData.getTable());

      BinlogEntry entry = extractor.extract(schemaAndTable, eventData, binlogFilename, offset, lastGTID);

      if (shouldProcessEvent(startingBinlogFileOffset, entry)) {
        cdcDataPublisherTransactionTemplate.inTransaction(() ->
          binlogEntryHandlers
              .stream()
              .filter(bh -> bh.isFor(schemaAndTable))
              .forEach(binlogEntryHandler ->
                  binlogEntryHandler.publish(cdcDataPublisher, entry)));

        offsetStore.save(new BinlogFileOffset(binlogFilename, offset, lastGTID));
      }
    }

    onEventReceived();
  }

  private boolean shouldProcessEvent(Optional<BinlogFileOffset> startingBinlogFileOffset, BinlogEntry entry) {
   return  (useGTIDsWhenPossible && isGTIDSupported()) ||
           !shouldSkipEntry(startingBinlogFileOffset, entry.getBinlogFileOffset());
  }

  private void handleUpdateRowsEvent(Event event) {
    UpdateRowsEventData eventData = event.getData();

    if (eventData == null) {
      return;
    }

    if (isCdcMonitoringTableId(eventData.getTableId())) {
      dbLogMetrics.onLagMeasurementEventReceived(timestampExtractor.extract(MONITORING_SCHEMA_AND_TABLE, eventData));
    }

    onEventReceived();
    saveOffset(event);
  }

  private long extractOffset(Event event) {
    return ((EventHeaderV4) event.getHeader()).getPosition();
  }

  private void saveOffset(Event event) {
    offset = extractOffset(event);
    BinlogFileOffset binlogFileOffset = new BinlogFileOffset(binlogFilename, offset, lastGTID);
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
            new WriteRowsDeserializer(tableMapEventByTableId, dbLogMetrics));

    eventDeserializer.setEventDataDeserializer(EventType.EXT_WRITE_ROWS,
            new WriteRowsDeserializer(tableMapEventByTableId, dbLogMetrics).setMayContainExtraInformation(true));

    eventDeserializer.setEventDataDeserializer(EventType.UPDATE_ROWS,
            new UpdateRowsDeserializer(tableMapEventByTableId, dbLogMetrics));

    eventDeserializer.setEventDataDeserializer(EventType.EXT_UPDATE_ROWS,
            new UpdateRowsDeserializer(tableMapEventByTableId, dbLogMetrics).setMayContainExtraInformation(true));

    eventDeserializer.setEventDataDeserializer(EventType.GTID,
            new GtidEventDataDeserializer());

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
    dataProducer.close();

    stopCountDownLatch.countDown();
  }

  private void saveEndingOffsetOfLastProcessedEvent(Event event) {
    long position = ((EventHeaderV4) event.getHeader()).getNextPosition();
    if (mySqlCdcProcessingStatusService != null) {
      mySqlCdcProcessingStatusService.saveEndingOffsetOfLastProcessedEvent(position);
    }
  }

  private DataSource createRootDataSource(String dbUrl, String dbUser, String dbPassword) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
    dataSource.setUrl(dbUrl);
    dataSource.setUsername(dbUser);
    dataSource.setPassword(dbPassword);
    return dataSource;
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
