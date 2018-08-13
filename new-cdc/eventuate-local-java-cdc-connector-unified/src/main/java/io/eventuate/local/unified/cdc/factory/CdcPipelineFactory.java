package io.eventuate.local.unified.cdc.factory;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.unified.cdc.pipeline.CdcPipeline;
import io.eventuate.local.unified.cdc.properties.CdcPipelineProperties;

public interface CdcPipelineFactory<PROPERTIES extends CdcPipelineProperties, EVENT extends BinLogEvent> {
  boolean supports(String type);
  Class<PROPERTIES> propertyClass();
  CdcPipeline<EVENT> create(PROPERTIES cdcPipelineProperties);
}
