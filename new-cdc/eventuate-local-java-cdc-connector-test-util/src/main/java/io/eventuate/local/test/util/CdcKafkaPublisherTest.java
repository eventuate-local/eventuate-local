package io.eventuate.local.test.util;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;


public abstract class CdcKafkaPublisherTest extends AbstractCdcTest {

  @Autowired
  protected EventuateJdbcAccess eventuateJdbcAccess;

  @Autowired
  protected EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;

  @Autowired
  protected CdcProcessor<PublishedEvent> cdcProcessor;

  @Autowired
  protected PublishingStrategy<PublishedEvent> publishingStrategy;

  protected EventuateLocalAggregateCrud localAggregateCrud;

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Test
  public void shouldSendPublishedEventsToKafka() throws InterruptedException {
    CdcDataPublisher<PublishedEvent> cdcDataPublisher = createCdcKafkaPublisher();

    cdcDataPublisher.start();

    cdcProcessor.start(cdcDataPublisher::handleEvent);

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);

    KafkaConsumer<String, String> consumer = createConsumer(eventuateKafkaConfigurationProperties.getBootstrapServers());
    consumer.partitionsFor(getEventTopicName());
    consumer.subscribe(Collections.singletonList(getEventTopicName()));

    waitForEventInKafka(consumer, entityIdVersionAndEventIds.getEntityId(), LocalDateTime.now().plusSeconds(40));
    cdcDataPublisher.stop();
  }

  protected abstract CdcDataPublisher<PublishedEvent> createCdcKafkaPublisher();
}
