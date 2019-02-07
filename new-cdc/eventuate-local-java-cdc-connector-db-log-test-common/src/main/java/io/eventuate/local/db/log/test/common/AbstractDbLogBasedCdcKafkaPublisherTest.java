package io.eventuate.local.db.log.test.common;

import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDbLogBasedCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Autowired
  private EventuateKafkaProducer eventuateKafkaProducer;

  @Override
  protected CdcDataPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new CdcDataPublisher<>(eventuateKafkaProducer, meterRegistry);
  }
}
