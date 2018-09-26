package io.eventuate.local.mysql.binlog;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.NullEventDataDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.WriteRowsEventDataDeserializer;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class MySqlBinaryLogClient extends DbLogClient {
  private String name;
  private BinaryLogClient client;
  private long binlogClientUniqueId;
  private final Map<Long, TableMapEventData> tableMapEventByTableId = new HashMap<>();
  private String binlogFilename;
  private long offset;
  private DataSource dataSource;
  private int connectionTimeoutInMilliseconds;
  private int maxAttemptsForBinlogConnection;
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  public MySqlBinaryLogClient(String dbUserName,
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
                              DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {

    super(dbUserName, dbPassword, dataSourceUrl, curatorFramework, leadershipLockPath, offsetStore);

    this.binlogClientUniqueId = binlogClientUniqueId;
    this.dataSource = dataSource;
    this.name = clientName;
    this.connectionTimeoutInMilliseconds = connectionTimeoutInMilliseconds;
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
    this.offsetStore = offsetStore;
    this.debeziumBinlogOffsetKafkaStore = debeziumBinlogOffsetKafkaStore;
  }

  public String getName() {
    return name;
  }

  @Override
  protected void leaderStart() {
    stopCountDownLatch = new CountDownLatch(1);
    running.set(true);

    client = new BinaryLogClient(host, port, dbUserName, dbPassword);
    client.setServerId(binlogClientUniqueId);
    client.setKeepAliveInterval(5 * 1000);

    Optional<BinlogFileOffset> binlogFileOffset = getStartingBinlogFileOffset();

    BinlogFileOffset bfo = binlogFileOffset.orElse(new BinlogFileOffset("", 4L));

    logger.debug("Starting with {}", bfo);
    client.setBinlogFilename(bfo.getBinlogFilename());
    client.setBinlogPosition(bfo.getOffset());

    client.setEventDeserializer(getEventDeserializer());
    client.registerEventListener(event -> {
      switch (event.getHeader().getEventType()) {
        case TABLE_MAP: {
          TableMapEventData tableMapEvent = event.getData();
          tableMapEventByTableId.put(tableMapEvent.getTableId(), tableMapEvent);
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

    while (running.get()) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        logger.error(e.getMessage(), e);
        running.set(false);
      }
    }

    stopCountDownLatch.countDown();
  }

  private Optional<BinlogFileOffset> getStartingBinlogFileOffset() {
    Optional<BinlogFileOffset> binlogFileOffset = offsetStore.getLastBinlogFileOffset();

    if (!binlogFileOffset.isPresent()) {
      binlogFileOffset = debeziumBinlogOffsetKafkaStore.getLastBinlogFileOffset();
    }

    return binlogFileOffset;
  }

  private void handleWriteRowsEvent(Event event, Optional<BinlogFileOffset> startingBinlogFileOffset) {
    logger.debug("Got binlog event {}", event);
    offset = ((EventHeaderV4) event.getHeader()).getPosition();
    WriteRowsEventData eventData = event.getData();
    if (tableMapEventByTableId.containsKey(eventData.getTableId())) {

      TableMapEventData tableMapEventData = tableMapEventByTableId.get(eventData.getTableId());

      String database = tableMapEventData.getDatabase();
      String table = tableMapEventData.getTable();

      binlogEntryHandlers
              .stream()
              .filter(bh -> bh.isFor(database, table, defaultDatabase))
              .forEach(new Consumer<BinlogEntryHandler>() {
                private boolean couldReadDuplicateEntries = true;

                @Override
                public void accept(BinlogEntryHandler binlogEntryHandler) {
                  try {
                    MySqlBinlogEntryExtractor extractor = new MySqlBinlogEntryExtractor(dataSource,
                            binlogEntryHandler.getSourceTableNameSupplier().getSourceTableName(),
                            binlogEntryHandler.getEventuateSchema());

                    BinlogEntry entry = extractor.extract(eventData, binlogFilename, offset);

                    if (couldReadDuplicateEntries) {
                      if (startingBinlogFileOffset.map(s -> s.isSameOrAfter(entry.getBinlogFileOffset())).orElse(false)) {
                        return;
                      } else {
                        couldReadDuplicateEntries = false;
                      }
                    }

                    binlogEntryHandler.publish(entry);

                  } catch (IOException e) {
                    throw new RuntimeException("Event row parsing exception", e);
                  }
                }
              });
    }
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

    // do not deserialize binlog events except the EXT_WRITE_ROWS, WRITE_ROWS, and TABLE_MAP
    Arrays.stream(EventType.values()).forEach(eventType -> {
      if (eventType != EventType.EXT_WRITE_ROWS &&
              eventType != EventType.TABLE_MAP &&
              eventType != EventType.WRITE_ROWS &&
              eventType != EventType.ROTATE) {
        eventDeserializer.setEventDataDeserializer(eventType,
                new NullEventDataDeserializer());
      }
    });

    eventDeserializer.setEventDataDeserializer(EventType.EXT_WRITE_ROWS,
            new WriteRowsEventDataDeserializer(
                    tableMapEventByTableId).setMayContainExtraInformation(true));

    eventDeserializer.setEventDataDeserializer(EventType.WRITE_ROWS,
            new WriteRowsEventDataDeserializer(
                    tableMapEventByTableId));

    return eventDeserializer;
  }

  @Override
  protected void leaderStop() {
    super.leaderStop();

    try {
      client.disconnect();
    } catch (IOException e) {
      logger.error("Cannot stop the MySqlBinaryLogClient", e);
    }
  }
}
