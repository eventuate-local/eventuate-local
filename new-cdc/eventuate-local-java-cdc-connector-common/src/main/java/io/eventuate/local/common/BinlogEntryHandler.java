package io.eventuate.local.common;

public class BinlogEntryHandler<EVENT extends BinLogEvent> {
  protected String eventuateSchema;
  protected String sourceTableName;
  protected BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;
  protected CdcDataPublisher<EVENT> cdcDataPublisher;

  public BinlogEntryHandler(String eventuateSchema,
                            String sourceTableName,
                            BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                            CdcDataPublisher<EVENT> cdcDataPublisher) {

    this.eventuateSchema = eventuateSchema;
    this.sourceTableName = sourceTableName;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
    this.cdcDataPublisher = cdcDataPublisher;
  }

  public String getQualifiedTable() {
    return String.format("%s.%s", eventuateSchema, sourceTableName);
  }

  public String getEventuateSchema() {
    return eventuateSchema;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }

  public boolean isFor(String requestedDatabase, String requestedTable) {
    return eventuateSchema.equals(requestedDatabase) && sourceTableName.equalsIgnoreCase(requestedTable);
  }

  public void publish(BinlogEntry binlogEntry) {
    cdcDataPublisher.handleEvent(binlogEntryToEventConverter.convert(binlogEntry));
  }
}
