package io.eventuate.local.unified.cdc.pipeline.common;

import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;

public class CommonPipelinePropertyValidationTest extends CommonPropertyValidationTest {
  protected  <PROPERTIES extends CdcPipelineProperties> void testCommonRequiredProperties(Class<PROPERTIES> propertyClass,
                                                                                          PropertyBuilder propertyBuilder) throws Exception {
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "reader must not be null");
    propertyBuilder.addString("reader", "reader1");
  }
}
