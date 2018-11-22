package io.eventuate.local.db.log.test.common;

import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.DuplicatePublishingDetector;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;

import java.util.Optional;


public abstract class AbstractDbLogBasedCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Override
  protected CdcDataPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new CdcDataPublisher<>(() ->
            new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(), EventuateKafkaProducerConfigurationProperties.empty()),
            new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(), EventuateKafkaConsumerConfigurationProperties.empty()),
            publishingStrategy,
            meterRegistry);
  }
}
