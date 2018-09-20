package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory.PostgresWalCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineReaderProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingDefaultCdcPipelinePropertiesConfiguration extends CommonCdcDefaultPipelinePropertiesConfiguration {

  @Profile("EventuatePolling")
  @Bean
  public CdcPipelineProperties defaultPollingPipelineProperties() {
    PollingPipelineProperties pollingPipelineProperties = createPollingPipelineProperties();

    initCdcPipelineProperties(pollingPipelineProperties);

    return pollingPipelineProperties;
  }

  @Profile("EventuatePolling")
  @Bean
  public CdcPipelineReaderProperties defaultPollingPipelineReaderProperties() {
    PollingPipelineReaderProperties pollingPipelineReaderProperties = createPollingPipelineReaderProperties();

    pollingPipelineReaderProperties.setType(PollingCdcPipelineReaderFactory.TYPE);

    initCdcPipelineReaderProperties(pollingPipelineReaderProperties);

    return pollingPipelineReaderProperties;
  }

  private PollingPipelineProperties createPollingPipelineProperties() {
    return new PollingPipelineProperties();
  }

  private PollingPipelineReaderProperties createPollingPipelineReaderProperties() {
    PollingPipelineReaderProperties pollingPipelineReaderProperties = new PollingPipelineReaderProperties();

    pollingPipelineReaderProperties.setPollingIntervalInMilliseconds(eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
    pollingPipelineReaderProperties.setMaxEventsPerPolling(eventuateConfigurationProperties.getMaxEventsPerPolling());
    pollingPipelineReaderProperties.setMaxAttemptsForPolling(eventuateConfigurationProperties.getMaxAttemptsForPolling());
    pollingPipelineReaderProperties.setPollingRetryIntervalInMilliseconds(eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds());

    return pollingPipelineReaderProperties;
  }
}
