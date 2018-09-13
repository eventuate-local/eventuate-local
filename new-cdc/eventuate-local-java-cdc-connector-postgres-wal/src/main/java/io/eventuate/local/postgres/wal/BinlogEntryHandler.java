package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntry;

import java.util.function.Consumer;

public class BinlogEntryHandler {
  private String defaultDatabase;
  private EventuateSchema eventuateSchema;
  private String sourceTableName;
  private Consumer<BinlogEntry> eventConsumer;

  public BinlogEntryHandler(String defaultDatabase,
                            EventuateSchema eventuateSchema,
                            String sourceTableName,
                            Consumer<BinlogEntry> eventConsumer) {

    this.defaultDatabase = defaultDatabase;
    this.eventuateSchema = eventuateSchema;
    this.sourceTableName = sourceTableName;
    this.eventConsumer = eventConsumer;
  }

  public String getDefaultDatabase() {
    return defaultDatabase;
  }

  public EventuateSchema getEventuateSchema() {
    return eventuateSchema;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }

  public Consumer<BinlogEntry> getEventConsumer() {
    return eventConsumer;
  }
}
