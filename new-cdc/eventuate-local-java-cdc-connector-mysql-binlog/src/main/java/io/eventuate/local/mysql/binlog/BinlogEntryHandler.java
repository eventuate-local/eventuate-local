package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntry;

import java.util.function.Consumer;

public class BinlogEntryHandler {
  private String defaultDatabase;
  private EventuateSchema eventuateSchema;
  private MySqlBinlogEntryExtractor mySqlBinlogEntryExtractor;
  private String sourceTableName;
  private Consumer<BinlogEntry> eventConsumer;

  public BinlogEntryHandler(String defaultDatabase,
                            EventuateSchema eventuateSchema,
                            MySqlBinlogEntryExtractor mySqlBinlogEntryExtractor,
                            String sourceTableName,
                            Consumer<BinlogEntry> eventConsumer) {

    this.defaultDatabase = defaultDatabase;
    this.eventuateSchema = eventuateSchema;
    this.mySqlBinlogEntryExtractor = mySqlBinlogEntryExtractor;
    this.sourceTableName = sourceTableName;
    this.eventConsumer = eventConsumer;
  }

  public String getDefaultDatabase() {
    return defaultDatabase;
  }

  public EventuateSchema getEventuateSchema() {
    return eventuateSchema;
  }

  public MySqlBinlogEntryExtractor getMySqlBinlogEntryExtractor() {
    return mySqlBinlogEntryExtractor;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }

  public Consumer<BinlogEntry> getEventConsumer() {
    return eventConsumer;
  }
}
