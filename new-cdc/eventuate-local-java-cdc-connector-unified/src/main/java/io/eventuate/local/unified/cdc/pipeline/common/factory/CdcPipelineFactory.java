package io.eventuate.local.unified.cdc.pipeline.common.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;

public class CdcPipelineFactory<EVENT extends BinLogEvent> {

  private String type;
  private BinlogEntryReaderProvider binlogEntryReaderProvider;
  private BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;
  private PublishingStrategy<EVENT> publishingStrategy;

  public CdcPipelineFactory(String type,
                            BinlogEntryReaderProvider binlogEntryReaderProvider,
                            BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                            PublishingStrategy<EVENT> publishingStrategy) {
    this.type = type;
    this.binlogEntryReaderProvider = binlogEntryReaderProvider;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
    this.publishingStrategy = publishingStrategy;
  }

  public boolean supports(String type) {
    return this.type.equals(type);
  }

  public CdcPipeline create(CdcPipelineProperties cdcPipelineProperties) {
    BinlogEntryReader binlogEntryReader = binlogEntryReaderProvider.getReader(cdcPipelineProperties.getReader());

    binlogEntryReader.addBinlogEntryHandler(new EventuateSchema(cdcPipelineProperties.getEventuateDatabaseSchema()),
            cdcPipelineProperties.getSourceTableName(),
            binlogEntryToEventConverter,
            publishingStrategy);

    return new CdcPipeline();
  }
}
