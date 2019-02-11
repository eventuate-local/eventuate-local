package io.eventuate.local.test.util;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;


public abstract class CdcKafkaPublisherTest extends AbstractCdcTest {

  @Autowired
  protected MeterRegistry meterRegistry;

  @Autowired
  protected EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;

  @Autowired
  protected PublishingStrategy<PublishedEvent> publishingStrategy;

  @Autowired
  protected EventuateSchema eventuateSchema;

  @Autowired
  protected SourceTableNameSupplier sourceTableNameSupplier;

  @Before
  public void init() {
    super.init();
  }

  @Test
  public void shouldSendPublishedEventsToKafka() {
    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(accountCreatedEventData);

    KafkaConsumer<String, String> consumer = createConsumer(eventuateKafkaConfigurationProperties.getBootstrapServers());
    consumer.partitionsFor(getEventTopicName());
    consumer.subscribe(Collections.singletonList(getEventTopicName()));

    waitForEventInKafka(consumer, entityIdVersionAndEventIds.getEntityId(), LocalDateTime.now().plusSeconds(40));
  }

  @After
  public abstract void clear();
}
