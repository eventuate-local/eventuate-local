package io.eventuate.local.polling;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.coordination.leadership.zookeeper.ZkLeaderSelector;
import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.common.*;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.local.test.util.SourceTableNameSupplier;
import io.eventuate.sql.dialect.SqlDialectConfiguration;
import io.eventuate.sql.dialect.SqlDialectSelector;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@Import({EventuateDriverConfiguration.class, SqlDialectConfiguration.class})
@EnableConfigurationProperties(EventuateKafkaProducerConfigurationProperties.class)
public class PollingIntegrationTestConfiguration {

  @Bean
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName() == null ? "events" : eventuateConfigurationProperties.getSourceTableName());
  }

  @Bean
  public EventuateConfigurationProperties eventuateConfigurationProperties() {
    return new EventuateConfigurationProperties();
  }

  @Bean
  public EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties() {
    return new EventuateLocalZookeperConfigurationProperties();
  }

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                       EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            eventuateKafkaProducerConfigurationProperties);
  }

  @Bean
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDataProvider pollingDataProvider() {
    return new EventPollingDataProvider();
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDao pollingDao(@Autowired(required = false) MeterRegistry meterRegistry,
                               @Value("${spring.datasource.url}") String dataSourceURL,
                               @Value("${spring.datasource.driver-class-name}") String driver,
                               EventuateConfigurationProperties eventuateConfigurationProperties,
                               DataSource dataSource,
                               LeaderSelectorFactory leaderSelectorFactory,
                               SqlDialectSelector sqlDialectSelector) {

    return new PollingDao(meterRegistry,
            dataSourceURL,
            dataSource,
            eventuateConfigurationProperties.getMaxEventsPerPolling(),
            eventuateConfigurationProperties.getMaxAttemptsForPolling(),
            eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPollingIntervalInMilliseconds(),
            eventuateConfigurationProperties.getLeadershipLockPath(),
            leaderSelectorFactory,
            eventuateConfigurationProperties.getReaderName(),
            sqlDialectSelector.getDialect(driver));
  }

  @Bean
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(eventuateLocalZookeperConfigurationProperties.getConnectionString())
            .build();
    client.start();
    return client;
  }

  @Bean
  public LeaderSelectorFactory connectorLeaderSelectorFactory(CuratorFramework curatorFramework) {
    return (lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback) ->
            new ZkLeaderSelector(curatorFramework, lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback);
  }

  @Bean
  public DuplicatePublishingDetector duplicatePublishingDetector(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                 EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            eventuateKafkaConsumerConfigurationProperties);
  }
}
