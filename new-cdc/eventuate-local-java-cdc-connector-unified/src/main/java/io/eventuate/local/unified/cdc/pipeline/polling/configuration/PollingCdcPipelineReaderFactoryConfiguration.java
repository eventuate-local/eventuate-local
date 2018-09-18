package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineReaderFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PollingCdcPipelineReaderFactoryConfiguration {
  @Bean("evenutateLocalPollingCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory pollingCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                                                  BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new PollingCdcPipelineReaderFactory(curatorFramework, binlogEntryReaderProvider);
  }
}
