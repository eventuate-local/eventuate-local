package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BinlogEntryHandler {
  protected EventuateSchema eventuateSchema;
  protected String sourceTableName;
  protected BiConsumer<BinlogEntry, Optional<BinlogFileOffset>> eventConsumer;

  public BinlogEntryHandler(EventuateSchema eventuateSchema,
                            String sourceTableName,
                            BiConsumer<BinlogEntry, Optional<BinlogFileOffset>> eventConsumer) {

    this.eventuateSchema = eventuateSchema;
    this.sourceTableName = sourceTableName;
    this.eventConsumer = eventConsumer;
  }

  public boolean isFor(String requestedDatabase, String requestedTable, String defaultDatabase) {
    boolean schemasAreEqual = eventuateSchema.isEmpty() && requestedDatabase.equalsIgnoreCase(defaultDatabase) ||
            requestedDatabase.equalsIgnoreCase(eventuateSchema.getEventuateDatabaseSchema());

    return schemasAreEqual && sourceTableName.equalsIgnoreCase(requestedTable);
  }
}
