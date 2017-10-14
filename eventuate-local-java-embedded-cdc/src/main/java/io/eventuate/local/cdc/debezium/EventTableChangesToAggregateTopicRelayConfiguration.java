package io.eventuate.local.cdc.debezium;

import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties({EventTableChangesToAggregateTopicRelayConfigurationProperties.class,
        EventuateKafkaConfigurationProperties.class,
        EventuateLocalZookeperConfigurationProperties.class,
        CdcStartupValidatorConfigurationProperties.class})
public class EventTableChangesToAggregateTopicRelayConfiguration {

  @Bean
  @Profile("!EventuatePolling")
  public EventTableChangesToAggregateTopicRelay embeddedDebeziumCDC(@Value("${spring.datasource.url}") String dataSourceURL,
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
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getLeadershipLockPath());
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
  @Profile("!EventuatePolling")
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
  public EventPollingDao eventPollingDao(DataSource dataSource,
    EventTableChangesToAggregateTopicRelayConfigurationProperties eventTableChangesToAggregateTopicRelayConfigurationProperties) {

    return new EventPollingDao(dataSource,
      eventTableChangesToAggregateTopicRelayConfigurationProperties.getMaxEventsPerPolling(),
      eventTableChangesToAggregateTopicRelayConfigurationProperties.getMaxAttemptsForPolling(),
      eventTableChangesToAggregateTopicRelayConfigurationProperties.getPollingRetryIntervalInMilliseconds());
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
