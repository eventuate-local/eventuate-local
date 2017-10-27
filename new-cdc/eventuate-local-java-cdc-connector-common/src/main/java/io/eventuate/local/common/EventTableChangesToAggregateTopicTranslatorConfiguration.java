package io.eventuate.local.common;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(EventuateLocalZookeperConfigurationProperties.class)
@Import(EventuateDriverConfiguration.class)
public class EventTableChangesToAggregateTopicTranslatorConfiguration {

  @Bean
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    String connectionString = eventuateLocalZookeperConfigurationProperties.getConnectionString();
    return makeStartedCuratorClient(connectionString);
  }

  @Bean
  public EventTableChangesToAggregateTopicTranslator<PublishedEvent> mySqlEventTableChangesToAggregateTopicTranslator(CdcKafkaPublisher<PublishedEvent> mySQLCdcKafkaPublisher,
          CdcProcessor<PublishedEvent> mySQLCdcProcessor,
          CuratorFramework curatorFramework,
          EventuateConfigurationProperties eventuateConfigurationProperties) {


    return new EventTableChangesToAggregateTopicTranslator<>(mySQLCdcKafkaPublisher,
            mySQLCdcProcessor,
            curatorFramework,
            eventuateConfigurationProperties.getLeadershipLockPath());
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
