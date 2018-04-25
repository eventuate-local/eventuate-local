package io.eventuate.local.db.log.common;

import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!EventuatePolling")
@Import(EventTableChangesToAggregateTopicTranslatorConfiguration.class)
public class CommonReplicationEventTableChangesToAggregateTopicTranslatorConfiguration {

  @Bean
  public DatabaseOffsetKafkaStore databaseOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                           EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                           EventuateKafkaProducer eventuateKafkaProducer) {

    return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties);
  }

  @Bean
  public CdcDataPublisher<PublishedEvent> cdcKafkaPublisher(DataProducerFactory dataProducerFactory,
                                                            EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                            DatabaseOffsetKafkaStore databaseOffsetKafkaStore,
                                                            PublishingStrategy<PublishedEvent> publishingStrategy) {

    return new DbLogBasedCdcDataPublisher<>(dataProducerFactory,
            databaseOffsetKafkaStore,
            eventuateKafkaConfigurationProperties.getBootstrapServers(),
            publishingStrategy);
  }

  @Bean
  public CdcProcessor<PublishedEvent> cdcProcessor(DbLogClient<PublishedEvent> dbLogClient,
                                                   DatabaseOffsetKafkaStore databaseOffsetKafkaStore) {

    return new DbLogBasedCdcProcessor<>(dbLogClient, databaseOffsetKafkaStore);
  }
}
