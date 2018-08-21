package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration;

import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration.CommonDbLogCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PostgresWalDefaultCdcPipelinePropertiesConfiguration extends CommonDbLogCdcDefaultPipelinePropertiesConfiguration {

  @Profile("PostgresWal")
  @Bean
  public CdcPipelineProperties defaultPostgresWalPipelineProperties() {
    PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties = createPostgresWalCdcPipelineProperties();

    initCommonDbLogCdcPipelineProperties(postgresWalCdcPipelineProperties);
    initCdcPipelineProperties(postgresWalCdcPipelineProperties);

    return postgresWalCdcPipelineProperties;
  }

  private PostgresWalCdcPipelineProperties createPostgresWalCdcPipelineProperties() {
    PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties = new PostgresWalCdcPipelineProperties();

    postgresWalCdcPipelineProperties.setPostgresReplicationStatusIntervalInMilliseconds(eventuateConfigurationProperties.getPostgresReplicationStatusIntervalInMilliseconds());
    postgresWalCdcPipelineProperties.setPostgresReplicationSlotName(eventuateConfigurationProperties.getPostgresReplicationSlotName());
    postgresWalCdcPipelineProperties.setPostgresWalIntervalInMilliseconds(eventuateConfigurationProperties.getPostgresWalIntervalInMilliseconds());

    return postgresWalCdcPipelineProperties;
  }
}
