package io.eventuate.local.unified.cdc.pipeline.common.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;

import java.util.function.Function;

public class CdcPipelineFactory<EVENT extends BinLogEvent> {

  private String type;
  private String readerType;
  private BinlogEntryReaderProvider binlogEntryReaderProvider;
  private CdcDataPublisher<EVENT> cdcDataPublisher;
  private Function<String, SourceTableNameSupplier> sourceTableNameSupplierFactory;
  private BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;

  public CdcPipelineFactory(String type,
                            String readerType,
                            BinlogEntryReaderProvider binlogEntryReaderProvider,
                            CdcDataPublisher<EVENT> cdcDataPublisher,
                            Function<String, SourceTableNameSupplier> sourceTableNameSupplierFactory,
                            BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter) {
    this.type = type;
    this.readerType = readerType;
    this.binlogEntryReaderProvider = binlogEntryReaderProvider;
    this.cdcDataPublisher = cdcDataPublisher;
    this.sourceTableNameSupplierFactory = sourceTableNameSupplierFactory;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
  }

  public boolean supports(String type, String readerType) {
    return this.type.equals(type) && this.readerType.equals(readerType);
  }

  public CdcPipeline<EVENT> create(CdcPipelineProperties cdcPipelineProperties) {
    BinlogEntryReader binlogEntryReader = binlogEntryReaderProvider.getReader(cdcPipelineProperties.getReader());

    binlogEntryReader.addBinlogEntryHandler(new EventuateSchema(cdcPipelineProperties.getEventuateDatabaseSchema()),
            sourceTableNameSupplierFactory.apply(cdcPipelineProperties.getSourceTableName()),
            binlogEntryToEventConverter,
            cdcDataPublisher);

    return new CdcPipeline<>(cdcDataPublisher);
  }
}
