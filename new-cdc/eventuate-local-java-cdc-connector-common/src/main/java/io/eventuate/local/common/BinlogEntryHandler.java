package io.eventuate.local.common;

public class BinlogEntryHandler<EVENT extends BinLogEvent> {
  protected SchemaAndTable schemaAndTable;
  protected BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;

  public BinlogEntryHandler(SchemaAndTable schemaAndTable,
                            BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter) {

    this.schemaAndTable = schemaAndTable;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
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

  public void publish(CdcDataPublisher cdcDataPublisher, BinlogEntry binlogEntry) {
    cdcDataPublisher.handleEvent(binlogEntryToEventConverter.convert(binlogEntry));
  }
}
