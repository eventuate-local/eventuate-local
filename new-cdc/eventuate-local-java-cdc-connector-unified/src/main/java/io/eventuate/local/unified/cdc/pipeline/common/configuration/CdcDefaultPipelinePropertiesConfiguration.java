package io.eventuate.local.unified.cdc.pipeline.common.configuration;

import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class CdcDefaultPipelinePropertiesConfiguration {
  @Value("${eventuate.database.schema:#{null}}")
  private String eventuateDataBaseSchema;

  @Autowired
  protected EventuateConfigurationProperties eventuateConfigurationProperties;

  @Bean
  public CdcPipelineProperties cdcPipelineProperties(SourceTableNameSupplier sourceTableNameSupplier) {
    CdcPipelineProperties cdcPipelineProperties = new CdcPipelineProperties();

    cdcPipelineProperties.setType("default");
    cdcPipelineProperties.setReader("default");
    cdcPipelineProperties.setEventuateDatabaseSchema(eventuateDataBaseSchema);
    cdcPipelineProperties.setSourceTableName(Optional
            .ofNullable(eventuateConfigurationProperties.getSourceTableName())
            .orElse(sourceTableNameSupplier.getSourceTableName()));

    return cdcPipelineProperties;
  }
}
