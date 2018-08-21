package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration;

import io.eventuate.local.common.MySqlBinlogCondition;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration.CommonDbLogCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySqlBinlogDefaultCdcPipelinePropertiesConfiguration extends CommonDbLogCdcDefaultPipelinePropertiesConfiguration {

  @Conditional(MySqlBinlogCondition.class)
  @Bean
  public CdcPipelineProperties defaultMySqlPipelineProperties() {
    MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties = createMySqlBinlogCdcPipelineProperties();

    initCommonDbLogCdcPipelineProperties(mySqlBinlogCdcPipelineProperties);
    initCdcPipelineProperties(mySqlBinlogCdcPipelineProperties);

    return mySqlBinlogCdcPipelineProperties;
  }

  private MySqlBinlogCdcPipelineProperties createMySqlBinlogCdcPipelineProperties() {
    MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties = new MySqlBinlogCdcPipelineProperties();

    mySqlBinlogCdcPipelineProperties.setCdcDbUserName(eventuateConfigurationProperties.getDbUserName());
    mySqlBinlogCdcPipelineProperties.setCdcDbPassword(eventuateConfigurationProperties.getDbPassword());
    mySqlBinlogCdcPipelineProperties.setSourceTableName(eventuateConfigurationProperties.getSourceTableName());
    mySqlBinlogCdcPipelineProperties.setBinlogClientId(eventuateConfigurationProperties.getBinlogClientId());
    mySqlBinlogCdcPipelineProperties.setOldDbHistoryTopicName(eventuateConfigurationProperties.getOldDbHistoryTopicName());

    return mySqlBinlogCdcPipelineProperties;
  }
}
