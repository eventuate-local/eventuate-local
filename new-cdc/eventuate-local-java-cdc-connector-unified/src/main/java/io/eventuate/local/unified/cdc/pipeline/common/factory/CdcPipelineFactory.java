package io.eventuate.local.unified.cdc.pipeline.common.factory;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;

public interface CdcPipelineFactory<EVENT extends BinLogEvent> {
  boolean supports(String type, String readerType);
  CdcPipeline<EVENT> create(CdcPipelineProperties cdcPipelineProperties);
}
