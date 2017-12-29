package io.eventuate.local.cdc.debezium;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.MySqlBinlogCondition;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class EventTableChangesToAggregateTopicRelayConfiguration {

  @Bean
  public EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties() {
    return new EventuateKafkaConfigurationProperties();
  }

  @Bean
  public EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties() {
    return new EventuateLocalZookeperConfigurationProperties();
  }

  @Bean
  public CdcStartupValidatorConfigurationProperties cdcStartupValidatorConfigurationProperties() {
    return new CdcStartupValidatorConfigurationProperties();
  }

  @Bean
  public EventTableChangesToAggregateTopicRelayConfigurationProperties eventTableChangesToAggregateTopicRelayConfigurationProperties() {
    return new EventTableChangesToAggregateTopicRelayConfigurationProperties();
  }

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public EventTableChangesToAggregateTopicRelay embeddedDebeziumCDC(EventuateSchema eventuateSchema,
    @Value("${spring.datasource.url}") String dataSourceURL,
    EventTableChangesToAggregateTopicRelayConfigurationProperties eventTableChangesToAggregateTopicRelayConfigurationProperties,
    EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
    CuratorFramework client,
    CdcStartupValidator cdcStartupValidator) {

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);

    return new MySqlBinLogBasedEventTableChangesToAggregateTopicRelay(
            eventuateKafkaConfigurationProperties.getBootstrapServers(),
            jdbcUrl,
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getDbUserName(),
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getDbPassword(),
            client,
            cdcStartupValidator,
            new TakeLeadershipAttemptTracker(eventTableChangesToAggregateTopicRelayConfigurationProperties.getMaxRetries(),
                    eventTableChangesToAggregateTopicRelayConfigurationProperties.getRetryPeriodInMilliseconds()),
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getLeadershipLockPath(),
            eventuateSchema);
  }

  @Bean
  @Profile("EventuatePolling")
  public EventTableChangesToAggregateTopicRelay pollingCDC(EventPollingDao eventPollingDao,
    EventTableChangesToAggregateTopicRelayConfigurationProperties eventTableChangesToAggregateTopicRelayConfigurationProperties,
    EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
    CuratorFramework client,
    CdcStartupValidator cdcStartupValidator) {

    return new PollingBasedEventTableChangesToAggregateTopicRelay(eventPollingDao,
        eventTableChangesToAggregateTopicRelayConfigurationProperties.getPollingIntervalInMilliseconds(),
        eventuateKafkaConfigurationProperties.getBootstrapServers(),
        client,
        cdcStartupValidator,
        new TakeLeadershipAttemptTracker(eventTableChangesToAggregateTopicRelayConfigurationProperties.getMaxRetries(),
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getRetryPeriodInMilliseconds()),
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getLeadershipLockPath()
            );
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public CdcStartupValidator debeziumCdcStartupValidator(@Value("${spring.datasource.url}") String dataSourceURL,
    EventTableChangesToAggregateTopicRelayConfigurationProperties eventTableChangesToAggregateTopicRelayConfigurationProperties,
    EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
    CdcStartupValidatorConfigurationProperties cdcStartupValidatorConfigurationProperties) {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);

    DebeziumCdcStartupValidator cdcStartupValidator = new DebeziumCdcStartupValidator(jdbcUrl,
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getDbUserName(),
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getDbPassword(),
            eventuateKafkaConfigurationProperties.getBootstrapServers());

    cdcStartupValidator.setMySqlValidationMaxAttempts(cdcStartupValidatorConfigurationProperties.getMySqlValidationMaxAttempts());
    cdcStartupValidator.setMySqlValidationTimeoutMillis(cdcStartupValidatorConfigurationProperties.getMySqlValidationTimeoutMillis());
    cdcStartupValidator.setKafkaValidationMaxAttempts(cdcStartupValidatorConfigurationProperties.getKafkaValidationMaxAttempts());
    cdcStartupValidator.setKafkaValidationTimeoutMillis(cdcStartupValidatorConfigurationProperties.getKafkaValidationTimeoutMillis());

    return cdcStartupValidator;
  }

  @Bean
  @Profile("EventuatePolling")
  public CdcStartupValidator basicCdcStartupValidator(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
    CdcStartupValidatorConfigurationProperties cdcStartupValidatorConfigurationProperties) {
    CdcStartupValidator cdcStartupValidator = new CdcStartupValidator(eventuateKafkaConfigurationProperties.getBootstrapServers());

    cdcStartupValidator.setKafkaValidationMaxAttempts(cdcStartupValidatorConfigurationProperties.getKafkaValidationMaxAttempts());
    cdcStartupValidator.setKafkaValidationTimeoutMillis(cdcStartupValidatorConfigurationProperties.getKafkaValidationTimeoutMillis());

    return cdcStartupValidator;
  }

  @Bean
  @Profile("EventuatePolling")
  public EventPollingDao eventPollingDao(EventuateSchema eventuateSchema,
    DataSource dataSource,
    EventTableChangesToAggregateTopicRelayConfigurationProperties eventTableChangesToAggregateTopicRelayConfigurationProperties) {

    return new EventPollingDao(dataSource,
      eventTableChangesToAggregateTopicRelayConfigurationProperties.getMaxEventsPerPolling(),
      eventTableChangesToAggregateTopicRelayConfigurationProperties.getMaxAttemptsForPolling(),
      eventTableChangesToAggregateTopicRelayConfigurationProperties.getPollingRetryIntervalInMilliseconds(),
      eventuateSchema);
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    String connectionString = eventuateLocalZookeperConfigurationProperties.getConnectionString();
    return makeStartedCuratorClient(connectionString);
  }

  static CuratorFramework makeStartedCuratorClient(String connectionString) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(connectionString)
            .build();
    client.start();
    return client;
  }
}
