package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineReaderFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingDefaultCdcPipelineReaderFactoryConfiguration {
  @Profile("EventuatePolling")
  @Bean("defaultCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory defaultPollingCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                                                   BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new PollingCdcPipelineReaderFactory(curatorFramework, binlogEntryReaderProvider);
  }
}
