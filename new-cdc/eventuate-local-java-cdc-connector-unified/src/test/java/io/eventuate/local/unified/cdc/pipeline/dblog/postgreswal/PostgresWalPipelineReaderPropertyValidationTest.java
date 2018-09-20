package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal;

import io.eventuate.local.unified.cdc.pipeline.dblog.common.CommonDbLogPipelineReaderPropertyValidationTest;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory.PostgresWalCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineReaderProperties;
import org.junit.Test;

public class PostgresWalPipelineReaderPropertyValidationTest extends CommonDbLogPipelineReaderPropertyValidationTest {
  @Test
  public void testPostgresWalProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();
    assertExceptionMessage(propertyBuilder.toString(), PostgresWalCdcPipelineReaderProperties.class, "type must not be null");

    propertyBuilder.addString("type", PostgresWalCdcPipelineReaderFactory.TYPE);
    testCommonRequiredProperties(PostgresWalCdcPipelineReaderProperties.class, propertyBuilder);

    assertNoException(propertyBuilder.toString(), PostgresWalCdcPipelineReaderProperties.class);

    PostgresWalCdcPipelineReaderProperties postgresWalCdcPipelineReaderProperties =
            objectMapper.readValue(propertyBuilder.toString(), PostgresWalCdcPipelineReaderProperties.class);

    testCommonDbLogDefaultProperties(postgresWalCdcPipelineReaderProperties);
  }
}
