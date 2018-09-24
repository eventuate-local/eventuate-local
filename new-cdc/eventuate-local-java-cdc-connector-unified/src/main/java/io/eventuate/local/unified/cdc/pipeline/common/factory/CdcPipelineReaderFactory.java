package io.eventuate.local.unified.cdc.pipeline.common.factory;

import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;

public interface CdcPipelineReaderFactory<PROPERTIES extends CdcPipelineReaderProperties, READER extends BinlogEntryReader> {
  boolean supports(String type);
  Class<PROPERTIES> propertyClass();
  READER create(PROPERTIES cdcPipelineReaderProperties);
}
