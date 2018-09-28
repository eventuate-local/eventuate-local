package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;

public class BinlogEntryHandler<EVENT extends BinLogEvent> {
  protected EventuateSchema eventuateSchema;
  protected String sourceTableName;
  protected BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;
  protected CdcDataPublisher<EVENT> cdcDataPublisher;

  public BinlogEntryHandler(EventuateSchema eventuateSchema,
                            String sourceTableName,
                            BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                            CdcDataPublisher<EVENT> cdcDataPublisher) {

    this.eventuateSchema = eventuateSchema;
    this.sourceTableName = sourceTableName;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
    this.cdcDataPublisher = cdcDataPublisher;
  }

  public String getQualifiedTable() {
    return eventuateSchema.qualifyTable(sourceTableName);
  }

  public EventuateSchema getEventuateSchema() {
    return eventuateSchema;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }

  public boolean isFor(String requestedDatabase, String requestedTable, String defaultDatabase) {
    boolean schemasAreEqual = eventuateSchema.isEmpty() && requestedDatabase.equalsIgnoreCase(defaultDatabase) ||
            requestedDatabase.equalsIgnoreCase(eventuateSchema.getEventuateDatabaseSchema());

    return schemasAreEqual && sourceTableName.equalsIgnoreCase(requestedTable);
  }

  public void publish(BinlogEntry binlogEntry) {
    cdcDataPublisher.handleEvent(binlogEntryToEventConverter.convert(binlogEntry));
  }
}
