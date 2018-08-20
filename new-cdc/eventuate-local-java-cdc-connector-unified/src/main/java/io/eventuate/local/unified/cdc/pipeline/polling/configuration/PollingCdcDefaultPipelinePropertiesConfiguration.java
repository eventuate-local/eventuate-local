package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingCdcDefaultPipelinePropertiesConfiguration extends CommonCdcDefaultPipelinePropertiesConfiguration {

  @Profile("EventuatePolling")
  @Bean
  public CdcPipelineProperties defaultPollingPipelineProperties() {
    PollingPipelineProperties pollingPipelineProperties = createPollingPipelineProperties();

    initCdcPipelineProperties(pollingPipelineProperties);

    return pollingPipelineProperties;
  }

  private PollingPipelineProperties createPollingPipelineProperties() {
    PollingPipelineProperties pollingPipelineProperties = new PollingPipelineProperties();

    pollingPipelineProperties.setPollingIntervalInMilliseconds(eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
    pollingPipelineProperties.setMaxEventsPerPolling(eventuateConfigurationProperties.getMaxEventsPerPolling());
    pollingPipelineProperties.setMaxAttemptsForPolling(eventuateConfigurationProperties.getMaxAttemptsForPolling());
    pollingPipelineProperties.setPollingRetryIntervalInMilliseconds(eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds());

    return pollingPipelineProperties;
  }
}
