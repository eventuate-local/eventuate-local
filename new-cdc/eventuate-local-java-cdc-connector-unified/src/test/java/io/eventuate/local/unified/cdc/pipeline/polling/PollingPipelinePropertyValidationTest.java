package io.eventuate.local.unified.cdc.pipeline.polling;

import io.eventuate.local.unified.cdc.pipeline.common.CommonPipelinePropertyValidationTest;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineFactory;
import org.junit.Test;

public class PollingPipelinePropertyValidationTest extends CommonPipelinePropertyValidationTest {

  @Test
  public void testPollingProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();

    assertExceptionMessage(propertyBuilder.toString(), PostgresWalCdcPipelineProperties.class, "type must not be null");

    propertyBuilder.addString("type", PollingCdcPipelineFactory.TYPE);
    testCommonRequiredProperties(PostgresWalCdcPipelineProperties.class, propertyBuilder);

    assertNoException(propertyBuilder.toString(), PostgresWalCdcPipelineProperties.class);
  }
}
