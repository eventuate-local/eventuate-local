package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.unified.cdc.pipeline.common.configuration.CdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.CdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcPipelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.DefaultCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcPipelineReaderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonCdcPipelineConfiguration.class,

        CdcDefaultPipelinePropertiesConfiguration.class,

        CdcPipelineFactoryConfiguration.class,
        DefaultCdcPipelineFactoryConfiguration.class,

        MySqlBinlogCdcPipelineReaderConfiguration.class,
        PollingCdcPipelineReaderConfiguration.class,
        PostgresWalCdcPipelineReaderConfiguration.class})
public class UnifiedCdcConnectorConfiguration {
  @Bean
  public CdcPipelineConfigurator cdcPipelineConfigurator() {
    return new CdcPipelineConfigurator();
  }
}
