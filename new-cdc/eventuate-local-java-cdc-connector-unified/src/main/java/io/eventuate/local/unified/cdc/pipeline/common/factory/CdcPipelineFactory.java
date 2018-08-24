package io.eventuate.local.unified.cdc.pipeline.common.factory;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;

public interface CdcPipelineFactory<PROPERTIES extends CdcPipelineProperties, EVENT extends BinLogEvent> {
  boolean supports(String type);
  Class<PROPERTIES> propertyClass();
  CdcPipeline<EVENT> create(PROPERTIES cdcPipelineProperties);
}
