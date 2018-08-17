package io.eventuate.local.unified.cdc;

import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.common.MySqlBinlogCondition;
import io.eventuate.local.unified.cdc.properties.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CdcDefaultPipelinePropertiesConfiguration {
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
  private DefaultCdcPipelineTypes defaultCdcPipelineTypes;

  @Autowired
  private EventuateConfigurationProperties eventuateConfigurationProperties;

  @Profile("EventuatePolling")
  @Bean
  public CdcPipelineProperties createDefaultPollingPipelineProperties() {
    PollingPipelineProperties pollingPipelineProperties = createPollingPipelineProperties();

    initCdcPipelineProperties(pollingPipelineProperties);

    return pollingPipelineProperties;
  }

  @Conditional(MySqlBinlogCondition.class)
  @Bean
  public CdcPipelineProperties createDefaultMySqlPipelineProperties() {
    MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties = createMySqlBinlogCdcPipelineProperties();

    initCommonDbLogCdcPipelineProperties(mySqlBinlogCdcPipelineProperties);
    initCdcPipelineProperties(mySqlBinlogCdcPipelineProperties);

    return mySqlBinlogCdcPipelineProperties;
  }

  @Profile("PostgresWal")
  @Bean
  public CdcPipelineProperties createDefaultPostgresWalPipelineProperties() {
    PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties = createPostgresWalCdcPipelineProperties();

    initCommonDbLogCdcPipelineProperties(postgresWalCdcPipelineProperties);
    initCdcPipelineProperties(postgresWalCdcPipelineProperties);

    return postgresWalCdcPipelineProperties;
  }

  private PollingPipelineProperties createPollingPipelineProperties() {
    PollingPipelineProperties pollingPipelineProperties = new PollingPipelineProperties();

    pollingPipelineProperties.setType(defaultCdcPipelineTypes.eventPollingPipelineType());
    pollingPipelineProperties.setPollingIntervalInMilliseconds(eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
    pollingPipelineProperties.setMaxEventsPerPolling(eventuateConfigurationProperties.getMaxEventsPerPolling());
    pollingPipelineProperties.setMaxAttemptsForPolling(eventuateConfigurationProperties.getMaxAttemptsForPolling());
    pollingPipelineProperties.setPollingRetryIntervalInMilliseconds(eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds());

    return pollingPipelineProperties;
  }

  private PostgresWalCdcPipelineProperties createPostgresWalCdcPipelineProperties() {
    PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties = new PostgresWalCdcPipelineProperties();

    postgresWalCdcPipelineProperties.setType(defaultCdcPipelineTypes.postgresWalPipelineType());
    postgresWalCdcPipelineProperties.setPostgresReplicationStatusIntervalInMilliseconds(eventuateConfigurationProperties.getPostgresReplicationStatusIntervalInMilliseconds());
    postgresWalCdcPipelineProperties.setPostgresReplicationSlotName(eventuateConfigurationProperties.getPostgresReplicationSlotName());
    postgresWalCdcPipelineProperties.setPostgresWalIntervalInMilliseconds(eventuateConfigurationProperties.getPostgresWalIntervalInMilliseconds());

    return postgresWalCdcPipelineProperties;
  }

  private MySqlBinlogCdcPipelineProperties createMySqlBinlogCdcPipelineProperties() {
    MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties = new MySqlBinlogCdcPipelineProperties();

    mySqlBinlogCdcPipelineProperties.setType(defaultCdcPipelineTypes.mySqlBinlogPipelineType());
    mySqlBinlogCdcPipelineProperties.setCdcDbUserName(eventuateConfigurationProperties.getDbUserName());
    mySqlBinlogCdcPipelineProperties.setCdcDbPassword(eventuateConfigurationProperties.getDbPassword());
    mySqlBinlogCdcPipelineProperties.setSourceTableName(eventuateConfigurationProperties.getSourceTableName());
    mySqlBinlogCdcPipelineProperties.setBinlogClientId(eventuateConfigurationProperties.getBinlogClientId());
    mySqlBinlogCdcPipelineProperties.setOldDbHistoryTopicName(eventuateConfigurationProperties.getOldDbHistoryTopicName());

    return mySqlBinlogCdcPipelineProperties;
  }

  private void initCommonDbLogCdcPipelineProperties(CommonDbLogCdcPipelineProperties commonDbLogCdcPipelineProperties) {
    commonDbLogCdcPipelineProperties.setBinlogConnectionTimeoutInMilliseconds(eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds());
    commonDbLogCdcPipelineProperties.setDbHistoryTopicName(eventuateConfigurationProperties.getDbHistoryTopicName());
    commonDbLogCdcPipelineProperties.setMaxAttemptsForBinlogConnection(eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());
    commonDbLogCdcPipelineProperties.setMySqlBinLogClientName(eventuateConfigurationProperties.getMySqlBinLogClientName());
  }

  private void initCdcPipelineProperties(CdcPipelineProperties cdcPipelineProperties) {
    cdcPipelineProperties.setDataSourceUrl(dataSourceURL);
    cdcPipelineProperties.setDataSourceUserName(dataSourceUserName);
    cdcPipelineProperties.setDataSourcePassword(dataSourcePassword);
    cdcPipelineProperties.setDataSourceDriverClassName(dataSourceDriverClassName);
    cdcPipelineProperties.setLeadershipLockPath(eventuateConfigurationProperties.getLeadershipLockPath());
    cdcPipelineProperties.setEventuateDatabaseSchema(eventuateDataBaseSchema);
  }
}
