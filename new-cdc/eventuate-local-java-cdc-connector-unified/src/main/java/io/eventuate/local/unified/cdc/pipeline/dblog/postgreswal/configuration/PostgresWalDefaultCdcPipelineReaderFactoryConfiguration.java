package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration;

import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory.PostgresWalCdcPipelineReaderFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PostgresWalDefaultCdcPipelineReaderFactoryConfiguration {
  @Profile("PostgresWal")
  @Bean("defaultCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory defaultPostgresWalCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                                                             BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new PostgresWalCdcPipelineReaderFactory(curatorFramework, binlogEntryReaderProvider);
  }
}
