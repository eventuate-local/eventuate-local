package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.CdcDataPublisherFactory;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcDefaultPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingCdcPipelineReaderConfiguration extends CommonCdcDefaultPipelineReaderConfiguration {

  @Bean("eventuateLocalPollingCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory pollingCdcPipelineReaderFactory(DataProducerFactory dataProducerFactory,
                                                                  CdcDataPublisherFactory cdcDataPublisherFactory,
                                                                  MeterRegistry meterRegistry,
                                                                  CuratorFramework curatorFramework,
                                                                  BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new PollingCdcPipelineReaderFactory(dataProducerFactory, cdcDataPublisherFactory, meterRegistry, curatorFramework, binlogEntryReaderProvider);
  }

  @Profile("EventuatePolling")
  @Bean("defaultCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory defaultPollingCdcPipelineReaderFactory(DataProducerFactory dataProducerFactory,
                                                                         CdcDataPublisherFactory cdcDataPublisherFactory,
                                                                         MeterRegistry meterRegistry,
                                                                         CuratorFramework curatorFramework,
                                                                         BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new PollingCdcPipelineReaderFactory(dataProducerFactory, cdcDataPublisherFactory, meterRegistry, curatorFramework, binlogEntryReaderProvider);
  }

  @Profile("EventuatePolling")
  @Bean
  public CdcPipelineReaderProperties defaultPollingPipelineReaderProperties() {
    PollingPipelineReaderProperties pollingPipelineReaderProperties = createPollingPipelineReaderProperties();

    pollingPipelineReaderProperties.setType(PollingCdcPipelineReaderFactory.TYPE);

    initCdcPipelineReaderProperties(pollingPipelineReaderProperties);

    return pollingPipelineReaderProperties;
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
