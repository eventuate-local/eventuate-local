package io.eventuate.local.polling;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;

public class PollingEntryHandler<EVENT extends BinLogEvent> extends BinlogEntryHandler<EVENT> {

  private String publishedField;
  private String idField;

  public PollingEntryHandler(EventuateSchema eventuateSchema,
                             String sourceTableName,
                             String publishedField,
                             String idField,
                             BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                             CdcDataPublisher<EVENT> dataPublisher) {

    super(eventuateSchema, sourceTableName, binlogEntryToEventConverter, dataPublisher);

    this.publishedField = publishedField;
    this.idField = idField;
  }

  public String getQualifiedTable() {
    return eventuateSchema.qualifyTable(sourceTableName);
  }

  public void accept(BinlogEntry binlogEntry) {
    cdcDataPublisher.handleEvent(binlogEntryToEventConverter.convert(binlogEntry));
  }

  public EventuateSchema getEventuateSchema() {
    return eventuateSchema;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }

  public String getPublishedField() {
    return publishedField;
  }

  public String getIdField() {
    return idField;
  }
}
