package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.unified.cdc.pipeline.common.configuration.*;
import io.eventuate.local.unified.cdc.pipeline.common.properties.RawUnifiedCdcProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcPipelineReaderConfiguration;
import io.eventuate.sql.dialect.SqlDialectConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SqlDialectConfiguration.class,

        CommonCdcPipelineConfiguration.class,

        CdcDataPublisherConfiguration.class,

        CdcDefaultPipelinePropertiesConfiguration.class,

        CdcPipelineFactoryConfiguration.class,
        DefaultCdcPipelineFactoryConfiguration.class,

        MySqlBinlogCdcPipelineReaderConfiguration.class,
        PollingCdcPipelineReaderConfiguration.class,
        PostgresWalCdcPipelineReaderConfiguration.class})
@EnableConfigurationProperties(RawUnifiedCdcProperties.class)
public class UnifiedCdcConnectorConfiguration {
  @Bean
  public CdcPipelineConfigurator cdcPipelineConfigurator() {
    return new CdcPipelineConfigurator();
  }
}
