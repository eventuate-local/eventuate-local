package io.eventuate.local.polling;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.common.*;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties({EventuateConfigurationProperties.class, EventuateLocalZookeperConfigurationProperties.class})
@Import(EventuateDriverConfiguration.class)
public class PollingIntegrationTestConfiguration {

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
  }

  @Bean
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }

  @Bean
  @Profile("EventuatePolling")
  public CdcProcessor<PublishedEvent> pollingCdcProcessor(EventuateConfigurationProperties eventuateConfigurationProperties,
          PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao) {
    
    return new PollingCdcProcessor<>(pollingDao, eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDataProvider<PublishedEventBean, PublishedEvent, String> pollingDataProvider(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new EventPollingDataProvider(eventuateConfigurationProperties.getEventuateDatabase());
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao(EventuateConfigurationProperties eventuateConfigurationProperties,
          PollingDataProvider<PublishedEventBean, PublishedEvent, String> pollingDataProvider,
    DataSource dataSource) {

    return new PollingDao<>(pollingDataProvider,
      dataSource,
      eventuateConfigurationProperties.getMaxEventsPerPolling(),
      eventuateConfigurationProperties.getMaxAttemptsForPolling(),
      eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds());
  }
}
