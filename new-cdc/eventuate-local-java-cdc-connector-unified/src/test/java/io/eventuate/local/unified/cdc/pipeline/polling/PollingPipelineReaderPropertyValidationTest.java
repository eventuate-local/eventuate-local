package io.eventuate.local.unified.cdc.pipeline.polling;

import io.eventuate.local.unified.cdc.pipeline.common.CommonPipelineReaderPropertyValidationTest;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineReaderProperties;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineReaderProperties;
import org.junit.Assert;
import org.junit.Test;

public class PollingPipelineReaderPropertyValidationTest extends CommonPipelineReaderPropertyValidationTest {

  @Test
  public void testPollingProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();

    assertExceptionMessage(propertyBuilder.toString(), PostgresWalCdcPipelineReaderProperties.class, "type must not be null");

    propertyBuilder.addString("type", PollingCdcPipelineReaderFactory.TYPE);
    testCommonRequiredProperties(PostgresWalCdcPipelineReaderProperties.class, propertyBuilder);

    assertNoException(propertyBuilder.toString(), PostgresWalCdcPipelineReaderProperties.class);

    PollingPipelineReaderProperties pollingPipelineReaderProperties =
            objectMapper.readValue(propertyBuilder.toString(), PollingPipelineReaderProperties.class);

    Assert.assertEquals(500, (int)pollingPipelineReaderProperties.getPollingIntervalInMilliseconds());
    Assert.assertEquals(1000, (int)pollingPipelineReaderProperties.getMaxEventsPerPolling());
    Assert.assertEquals(100, (int)pollingPipelineReaderProperties.getMaxAttemptsForPolling());
    Assert.assertEquals(500, (int)pollingPipelineReaderProperties.getPollingRetryIntervalInMilliseconds());
  }
}
