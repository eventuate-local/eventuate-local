package io.eventuate.local.polling;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.function.Consumer;

@Configuration
@EnableAutoConfiguration
@Import(EventuateDriverConfiguration.class)
@EnableConfigurationProperties(EventuateKafkaProducerConfigurationProperties.class)
public class PollingIntegrationTestConfiguration {

  @Bean
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName(), "events");
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
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
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
  public CdcProcessor<PublishedEvent> pollingCdcProcessor(@Value("${spring.datasource.url}") String dbUrl,
                                                          EventuateConfigurationProperties eventuateConfigurationProperties,
                                                          PollingDao pollingDao,
                                                          PollingDataProvider pollingDataProvider,
                                                          EventuateSchema eventuateSchema,
                                                          SourceTableNameSupplier sourceTableNameSupplier) {
    
    return new PollingCdcProcessor<PublishedEvent>(pollingDao,
            pollingDataProvider,
            new BinlogEntryToPublishedEventConverter(),
            eventuateSchema,
            sourceTableNameSupplier.getSourceTableName()) {
      @Override
      public void start(Consumer<PublishedEvent> publishedEventConsumer) {
        super.start(publishedEventConsumer);
        pollingDao.start();
      }

      @Override
      public void stop() {
        pollingDao.stop();
        super.stop();
      }
    };
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDataProvider pollingDataProvider() {
    return new EventPollingDataProvider();
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDao pollingDao(EventuateConfigurationProperties eventuateConfigurationProperties,
                               DataSource dataSource,
                               CuratorFramework curatorFramework) {

    return new PollingDao(dataSource,
            eventuateConfigurationProperties.getMaxEventsPerPolling(),
            eventuateConfigurationProperties.getMaxAttemptsForPolling(),
            eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPollingIntervalInMilliseconds(),
            curatorFramework,
            eventuateConfigurationProperties.getLeadershipLockPath());
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
}
