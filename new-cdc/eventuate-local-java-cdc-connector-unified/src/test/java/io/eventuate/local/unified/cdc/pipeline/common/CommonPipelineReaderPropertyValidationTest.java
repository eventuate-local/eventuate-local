package io.eventuate.local.unified.cdc.pipeline.common;

import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;

public class CommonPipelineReaderPropertyValidationTest extends CommonPropertyValidationTest {
  protected  <PROPERTIES extends CdcPipelineReaderProperties> void testCommonRequiredProperties(Class<PROPERTIES> propertyClass,
                                                                                                PropertyBuilder propertyBuilder) throws Exception {

    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "name must not be null");
    propertyBuilder.addString("name", "reader1");

    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourceUrl must not be null");

    propertyBuilder.addString("dataSourceUrl", "jdbc:correctdb://localhost/eventuate");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourceUserName must not be null");

    propertyBuilder.addString("dataSourceUserName", "testUser");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourcePassword must not be null");

    propertyBuilder.addString("dataSourcePassword", "testPassword");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourceDriverClassName must not be null");

    propertyBuilder.addString("dataSourceDriverClassName", "com.correct.db.Driver");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "leadershipLockPath must not be null");

    propertyBuilder.addString("leadershipLockPath", "/eventuate/leader/test");
  }
}
