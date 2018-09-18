package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration;

import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.MySqlBinlogCdcPipelineReaderFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySqlBinlogCdcPipelineReaderFactoryConfiguration {
  @Bean("eventuateLocalMySqlBinlogCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory mySqlBinlogCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                                                      BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new MySqlBinlogCdcPipelineReaderFactory(curatorFramework, binlogEntryReaderProvider);
  }
}
