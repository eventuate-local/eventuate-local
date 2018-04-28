package io.eventuate.local.db.log.common;

import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.context.annotation.*;

@Configuration
@Profile("!EventuatePolling")
@Import(EventTableChangesToAggregateTopicTranslatorConfiguration.class)
public class CommonReplicationEventTableChangesToAggregateTopicTranslatorConfiguration {

  @Bean
  @Primary
  public OffsetStore databaseOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
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
                                                            OffsetStore offsetStore,
                                                            PublishingStrategy<PublishedEvent> publishingStrategy) {

    return new DbLogBasedCdcDataPublisher<>(dataProducerFactory,
            offsetStore,
            new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers()),
            publishingStrategy);
  }
}
