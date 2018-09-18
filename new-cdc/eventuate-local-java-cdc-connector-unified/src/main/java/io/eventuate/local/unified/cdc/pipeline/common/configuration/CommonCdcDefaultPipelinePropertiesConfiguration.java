package io.eventuate.local.unified.cdc.pipeline.common.configuration;

import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class CommonCdcDefaultPipelinePropertiesConfiguration {
  @Value("${spring.profiles.active:#{\"\"}}")
  private String springProfilesActive;

  @Value("${spring.datasource.url:#{null}}")
  private String dataSourceURL;

  @Value("${spring.datasource.username:#{null}}")
  private String dataSourceUserName;

  @Value("${spring.datasource.password:#{null}}")
  private String dataSourcePassword;

  @Value("${spring.datasource.driver.class.name:#{null}}")
  private String dataSourceDriverClassName;

  @Value("${eventuate.database.schema:#{null}}")
  private String eventuateDataBaseSchema;

  @Autowired
  protected EventuateConfigurationProperties eventuateConfigurationProperties;

  protected void initCdcPipelineProperties(CdcPipelineProperties cdcPipelineProperties) {
    cdcPipelineProperties.setType("default");
    cdcPipelineProperties.setReader("default");
    cdcPipelineProperties.setDataSourceUrl(dataSourceURL);
    cdcPipelineProperties.setDataSourceUserName(dataSourceUserName);
    cdcPipelineProperties.setDataSourcePassword(dataSourcePassword);
    cdcPipelineProperties.setDataSourceDriverClassName(dataSourceDriverClassName);
    cdcPipelineProperties.setLeadershipLockPath(eventuateConfigurationProperties.getLeadershipLockPath());
    cdcPipelineProperties.setEventuateDatabaseSchema(eventuateDataBaseSchema);
  }

  protected void initCdcPipelineReaderProperties(CdcPipelineReaderProperties cdcPipelineReaderProperties) {
    cdcPipelineReaderProperties.setType("default");
    cdcPipelineReaderProperties.setName("default");
    cdcPipelineReaderProperties.setDataSourceUrl(dataSourceURL);
    cdcPipelineReaderProperties.setDataSourceUserName(dataSourceUserName);
    cdcPipelineReaderProperties.setDataSourcePassword(dataSourcePassword);
    cdcPipelineReaderProperties.setDataSourceDriverClassName(dataSourceDriverClassName);
    cdcPipelineReaderProperties.setLeadershipLockPath(eventuateConfigurationProperties.getLeadershipLockPath());
    cdcPipelineReaderProperties.setEventuateDatabaseSchema(eventuateDataBaseSchema);
  }
}
