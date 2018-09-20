package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration;

import io.eventuate.local.common.MySqlBinlogCondition;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration.CommonDbLogCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.MySqlBinlogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineReaderProperties;
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

  @Conditional(MySqlBinlogCondition.class)
  @Bean
  public CdcPipelineReaderProperties defaultMySqlPipelineReaderProperties() {
    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties = createMySqlBinlogCdcPipelineReaderProperties();

    mySqlBinlogCdcPipelineReaderProperties.setType(MySqlBinlogCdcPipelineReaderFactory.TYPE);

    initCommonDbLogCdcPipelineReaderProperties(mySqlBinlogCdcPipelineReaderProperties);
    initCdcPipelineReaderProperties(mySqlBinlogCdcPipelineReaderProperties);

    return mySqlBinlogCdcPipelineReaderProperties;
  }

  private MySqlBinlogCdcPipelineProperties createMySqlBinlogCdcPipelineProperties() {
    MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties = new MySqlBinlogCdcPipelineProperties();

    mySqlBinlogCdcPipelineProperties.setSourceTableName(eventuateConfigurationProperties.getSourceTableName());
    mySqlBinlogCdcPipelineProperties.setOldDbHistoryTopicName(eventuateConfigurationProperties.getOldDbHistoryTopicName());

    return mySqlBinlogCdcPipelineProperties;
  }

  private MySqlBinlogCdcPipelineReaderProperties createMySqlBinlogCdcPipelineReaderProperties() {
    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties = new MySqlBinlogCdcPipelineReaderProperties();

    mySqlBinlogCdcPipelineReaderProperties.setCdcDbUserName(eventuateConfigurationProperties.getDbUserName());
    mySqlBinlogCdcPipelineReaderProperties.setCdcDbPassword(eventuateConfigurationProperties.getDbPassword());
    mySqlBinlogCdcPipelineReaderProperties.setBinlogClientId(eventuateConfigurationProperties.getBinlogClientId());

    return mySqlBinlogCdcPipelineReaderProperties;
  }
}
