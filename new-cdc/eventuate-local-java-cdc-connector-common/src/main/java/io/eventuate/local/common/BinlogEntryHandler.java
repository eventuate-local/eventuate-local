package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;

import java.util.function.Consumer;

public abstract class BinlogEntryHandler {
  protected String defaultDatabase;
  protected EventuateSchema eventuateSchema;
  protected String sourceTableName;
  protected Consumer<BinlogEntry> eventConsumer;

  public BinlogEntryHandler(String defaultDatabase,
                            EventuateSchema eventuateSchema,
                            String sourceTableName,
                            Consumer<BinlogEntry> eventConsumer) {

    this.defaultDatabase = defaultDatabase;
    this.eventuateSchema = eventuateSchema;
    this.sourceTableName = sourceTableName;
    this.eventConsumer = eventConsumer;
  }

  public boolean isFor(String database, String table) {
    boolean schemasAreEqual = eventuateSchema.isEmpty() && database.equalsIgnoreCase(defaultDatabase) ||
            database.equalsIgnoreCase(eventuateSchema.getEventuateDatabaseSchema());

    return schemasAreEqual && sourceTableName.equalsIgnoreCase(table);
  }
}
