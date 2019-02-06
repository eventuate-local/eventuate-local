package io.eventuate.local.unified.cdc.pipeline.common.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.common.BinlogEntryToEventConverter;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;

public class CdcPipelineFactory<EVENT extends BinLogEvent> {

  private String type;
  private BinlogEntryReaderProvider binlogEntryReaderProvider;
  private BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;

  public CdcPipelineFactory(String type,
                            BinlogEntryReaderProvider binlogEntryReaderProvider,
                            BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter) {
    this.type = type;
    this.binlogEntryReaderProvider = binlogEntryReaderProvider;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
  }

  public boolean supports(String type) {
    return this.type.equals(type);
  }

  public CdcPipeline create(CdcPipelineProperties cdcPipelineProperties) {
    BinlogEntryReader binlogEntryReader = binlogEntryReaderProvider.getReader(cdcPipelineProperties.getReader());

    binlogEntryReader.addBinlogEntryHandler(new EventuateSchema(cdcPipelineProperties.getEventuateDatabaseSchema()),
            cdcPipelineProperties.getSourceTableName(),
            binlogEntryToEventConverter);

    return new CdcPipeline();
  }
}
