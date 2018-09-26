package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;

public class BinlogEntryHandler<EVENT extends BinLogEvent> {
  protected EventuateSchema eventuateSchema;
  protected SourceTableNameSupplier sourceTableNameSupplier;
  protected BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;
  protected CdcDataPublisher<EVENT> cdcDataPublisher;

  public BinlogEntryHandler(EventuateSchema eventuateSchema,
                            SourceTableNameSupplier sourceTableNameSupplier,
                            BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                            CdcDataPublisher<EVENT> cdcDataPublisher) {

    this.eventuateSchema = eventuateSchema;
    this.sourceTableNameSupplier = sourceTableNameSupplier;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
    this.cdcDataPublisher = cdcDataPublisher;
  }

  public String getQualifiedTable() {
    return eventuateSchema.qualifyTable(sourceTableNameSupplier.getSourceTableName());
  }

  public EventuateSchema getEventuateSchema() {
    return eventuateSchema;
  }

  public SourceTableNameSupplier getSourceTableNameSupplier() {
    return sourceTableNameSupplier;
  }

  public CdcDataPublisher<EVENT> getCdcDataPublisher() {
    return cdcDataPublisher;
  }

  public boolean isFor(String requestedDatabase, String requestedTable, String defaultDatabase) {
    boolean schemasAreEqual = eventuateSchema.isEmpty() && requestedDatabase.equalsIgnoreCase(defaultDatabase) ||
            requestedDatabase.equalsIgnoreCase(eventuateSchema.getEventuateDatabaseSchema());

    return schemasAreEqual && sourceTableNameSupplier.getSourceTableName().equalsIgnoreCase(requestedTable);
  }

  public void publish(BinlogEntry binlogEntry) {
    cdcDataPublisher.handleEvent(binlogEntryToEventConverter.convert(binlogEntry));
  }
}
