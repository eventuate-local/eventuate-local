package io.eventuate.local.unified.cdc.pipeline.common;

import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import org.junit.Test;

public class CommonPipelinePropertyValidationTest extends CommonPropertyValidationTest {

  @Test
  public void testMySqlBinlogProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();

    assertExceptionMessage(propertyBuilder.toString(), CdcPipelineProperties.class, "type must not be null");

    propertyBuilder.addString("type", "some type");

    assertExceptionMessage(propertyBuilder.toString(), CdcPipelineProperties.class, "reader must not be null");
    propertyBuilder.addString("reader", "reader1");

    assertNoException(propertyBuilder.toString(), CdcPipelineProperties.class);
  }
}
