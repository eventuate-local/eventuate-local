package io.eventuate.local.unified.cdc.pipeline.polling;

import io.eventuate.local.unified.cdc.pipeline.common.CommonPropertyValidationTest;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineProperties;
import org.junit.Assert;
import org.junit.Test;

public class PollingPropertyValidationTest extends CommonPropertyValidationTest {

  @Test
  public void testPollingProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();
    assertExceptionMessage(propertyBuilder.toString(), PostgresWalCdcPipelineProperties.class, "type must not be null");

    propertyBuilder.addString("type", PollingCdcPipelineFactory.TYPE);
    testCommonRequiredProperties(PostgresWalCdcPipelineProperties.class, propertyBuilder);

    assertNoException(propertyBuilder.toString(), PostgresWalCdcPipelineProperties.class);

    PollingPipelineProperties pollingPipelineProperties = objectMapper.readValue(propertyBuilder.toString(), PollingPipelineProperties.class);

    Assert.assertEquals(500, (int)pollingPipelineProperties.getPollingIntervalInMilliseconds());
    Assert.assertEquals(1000, (int)pollingPipelineProperties.getMaxEventsPerPolling());
    Assert.assertEquals(100, (int)pollingPipelineProperties.getMaxAttemptsForPolling());
    Assert.assertEquals(500, (int)pollingPipelineProperties.getPollingRetryIntervalInMilliseconds());
  }
}
