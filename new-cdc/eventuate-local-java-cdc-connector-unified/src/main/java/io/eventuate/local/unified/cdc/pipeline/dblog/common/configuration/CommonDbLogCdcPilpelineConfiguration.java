package io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration;

import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonDbLogCdcPilpelineConfiguration {

  @Bean
  public BinlogEntryReaderProvider dbClientProvider() {
    return new BinlogEntryReaderProvider();
  }
}
