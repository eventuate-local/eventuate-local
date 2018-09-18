package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingDefaultCdcPipelineFactoryConfiguration {
  @Profile("EventuatePolling")
  @Bean("defaultCdcPipelineFactory")
  public CdcPipelineFactory defaultPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                             DataProducerFactory dataProducerFactory,
                                                             BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new PollingCdcPipelineFactory(curatorFramework, dataProducerFactory, binlogEntryReaderProvider);
  }
}
