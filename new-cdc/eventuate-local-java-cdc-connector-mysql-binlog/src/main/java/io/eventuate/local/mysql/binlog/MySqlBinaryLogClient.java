package io.eventuate.local.mysql.binlog;


import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.NullEventDataDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.WriteRowsEventDataDeserializer;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.db.log.common.DbLogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MySqlBinaryLogClient implements DbLogClient {

  private String name;

  private BinaryLogClient client;
  private long binlogClientUniqueId;

  private final String dbUserName;
  private final String dbPassword;
  private final String host;
  private final int port;

  private final Map<Long, TableMapEventData> tableMapEventByTableId = new HashMap<>();
  private String binlogFilename;
  private long offset;

  private int connectionTimeoutInMilliseconds;
  private int maxAttemptsForBinlogConnection;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private List<BinlogEntryHandler> binlogEntryHandlers = new CopyOnWriteArrayList<>();

  private AtomicBoolean started = new AtomicBoolean(false);

  private Optional<BinlogFileOffset> binlogFileOffset = Optional.empty();

  public MySqlBinaryLogClient(String dbUserName,
                              String dbPassword,
                              String host,
                              int port,
                              long binlogClientUniqueId,
                              String clientName,
                              int connectionTimeoutInMilliseconds,
                              int maxAttemptsForBinlogConnection) {
    this.binlogClientUniqueId = binlogClientUniqueId;
    this.dbUserName = dbUserName;
    this.dbPassword = dbPassword;
    this.host = host;
    this.port = port;
    this.name = clientName;
    this.connectionTimeoutInMilliseconds = connectionTimeoutInMilliseconds;
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
  }

  public void addBinlogEntryHandler(BinlogEntryHandler binlogEntryHandler) {
    binlogEntryHandlers.add(binlogEntryHandler);
  }

  public void setBinlogFileOffset(Optional<BinlogFileOffset> binlogFileOffset) {
    this.binlogFileOffset = binlogFileOffset;
  }

  public void start() {
    if (!started.compareAndSet(false, true)) {
      return;
    }

    client = new BinaryLogClient(host, port, dbUserName, dbPassword);
    client.setServerId(binlogClientUniqueId);
    client.setKeepAliveInterval(5 * 1000);

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
          handleWriteRowsEvent(event);
          break;
        }
        case WRITE_ROWS: {
          handleWriteRowsEvent(event);
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
  }

  private void handleWriteRowsEvent(Event event) {
    logger.debug("Got binlog event {}", event);
    offset = ((EventHeaderV4) event.getHeader()).getPosition();
    WriteRowsEventData eventData = event.getData();
    if (tableMapEventByTableId.containsKey(eventData.getTableId())) {

      binlogEntryHandlers
              .stream()
              .filter(bh -> {
                TableMapEventData tableMapEventData = tableMapEventByTableId.get(eventData.getTableId());

                String database = tableMapEventData.getDatabase();
                String table = tableMapEventData.getTable();

                return checkSchemasAreEqual(bh, database) && bh.getSourceTableName().equalsIgnoreCase(table);
              })
              .forEach(bh -> {
                try {
                  MySqlBinlogEntryExtractor extractor = bh.getMySqlBinlogEntryExtractor();
                  Consumer<BinlogEntry> consumer = bh.getEventConsumer();

                  BinlogEntry entry = extractor.extract(eventData, getCurrentBinlogFilename(), offset);

                  consumer.accept(entry);
                } catch (IOException e) {
                  throw new RuntimeException("Event row parsing exception", e);
                }
              });
    }
  }

  private boolean checkSchemasAreEqual(BinlogEntryHandler binlogEntryHandler, String database) {
    return binlogEntryHandler.getEventuateSchema().isEmpty() && database.equalsIgnoreCase(binlogEntryHandler.getDefaultDatabase()) ||
            database.equalsIgnoreCase(binlogEntryHandler.getEventuateSchema().getEventuateDatabaseSchema());
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
          throw new RuntimeException(ex);
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

  public void stop() {
    if (!started.compareAndSet(true, false)) {
      return;
    }

    try {
      client.disconnect();
    } catch (IOException e) {
      logger.error("Cannot stop the MySqlBinaryLogClient", e);
    }

    binlogEntryHandlers.clear();
  }

  public String getCurrentBinlogFilename() {
    return this.binlogFilename;
  }

  public long getCurrentOffset() {
    return this.offset;
  }

  public String getName() {
    return name;
  }

}
