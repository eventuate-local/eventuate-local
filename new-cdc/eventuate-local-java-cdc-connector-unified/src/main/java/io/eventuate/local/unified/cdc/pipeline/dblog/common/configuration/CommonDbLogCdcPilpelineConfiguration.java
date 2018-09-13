package io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration;

import io.eventuate.local.unified.cdc.pipeline.dblog.common.DbLogClientProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonDbLogCdcPilpelineConfiguration {

  @Bean
  public DbLogClientProvider dbClientProvider() {
    return new DbLogClientProvider();
  }
}
