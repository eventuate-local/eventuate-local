package io.eventuate.local.common;

public class BinlogEntryHandler<EVENT extends BinLogEvent> {
  protected SchemaAndTable schemaAndTable;
  protected BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;
  protected CdcDataPublisher<EVENT> cdcDataPublisher;

  public BinlogEntryHandler(SchemaAndTable schemaAndTable,
                            BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                            CdcDataPublisher<EVENT> cdcDataPublisher) {

    this.schemaAndTable = schemaAndTable;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
    this.cdcDataPublisher = cdcDataPublisher;
  }

  public String getQualifiedTable() {
    return String.format("%s.%s", schemaAndTable.getSchema(), schemaAndTable.getTableName());
  }

  public SchemaAndTable getSchemaAndTable() {
    return schemaAndTable;
  }

  public boolean isFor(SchemaAndTable schemaAndTable) {
    return this.schemaAndTable.equals(schemaAndTable);
  }

  public void publish(BinlogEntry binlogEntry) {
    cdcDataPublisher.handleEvent(binlogEntryToEventConverter.convert(binlogEntry));
  }
}
