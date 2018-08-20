package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.DefaultPipelineTypeSupplier;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.DefaultPollingCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingCdcPipelineFactoryConfiguration {
  @Profile("EventuatePolling")
  @Bean
  public DefaultPipelineTypeSupplier defaultPipelineTypeSupplier() {
    return () -> DefaultPollingCdcPipelineFactory.TYPE;
  }

  @Bean
  public PollingCdcPipelineFactory pollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                             DataProducerFactory dataProducerFactory) {

    return new PollingCdcPipelineFactory(curatorFramework, dataProducerFactory);
  }

  @Bean
  public DefaultPollingCdcPipelineFactory defaultPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                           DataProducerFactory dataProducerFactory) {

    return new DefaultPollingCdcPipelineFactory(curatorFramework, dataProducerFactory);
  }
}
