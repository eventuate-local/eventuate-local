package io.eventuate.local.test.util;

import io.eventuate.EntityIdAndType;
import io.eventuate.Int128;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.example.banking.domain.AccountDebitedEvent;
import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AbstractCdcTest extends AbstractConnectorTest{

  @Autowired
  protected EventuateJdbcAccess eventuateJdbcAccess;

  protected EventuateLocalAggregateCrud localAggregateCrud;

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  public String generateAccountCreatedEvent() {
    return JSonMapper.toJson(new AccountCreatedEvent(new BigDecimal(System.currentTimeMillis())));
  }

  public String generateAccountDebitedEvent() {
    return JSonMapper.toJson(new AccountDebitedEvent(new BigDecimal(System.currentTimeMillis()), null));
  }

  public String getEventTopicName() {
    return Account.class.getTypeName();
  }

  public EntityIdVersionAndEventIds saveEvent(String eventData) {
    List<EventTypeAndData> events = Collections.singletonList(new EventTypeAndData(AccountCreatedEvent.class.getTypeName(), eventData, Optional.empty()));

    return localAggregateCrud.save(Account.class.getTypeName(), events, Optional.empty());
  }

  public EntityIdVersionAndEventIds updateEvent(String entityId, Int128 entityVersion, String eventData) {
    List<EventTypeAndData> events = Collections.singletonList(new EventTypeAndData(AccountCreatedEvent.class.getTypeName(), eventData, Optional.empty()));

    return localAggregateCrud.update(new EntityIdAndType(entityId, Account.class.getTypeName()),
            entityVersion,
            events,
            Optional.empty());
  }

  public PublishedEvent waitForEvent(BlockingQueue<PublishedEvent> publishedEvents, Int128 eventId, LocalDateTime deadline, String eventData) throws InterruptedException {
    while (LocalDateTime.now().isBefore(deadline)) {
      long millis = ChronoUnit.MILLIS.between(deadline, LocalDateTime.now());
      PublishedEvent event = publishedEvents.poll(millis, TimeUnit.MILLISECONDS);
      if (event != null && event.getId().equals(eventId.asString()) && eventData.equals(event.getEventData()))
        return event;
    }
    throw new RuntimeException("event not found: " + eventId);
  }

  public void waitForEventInKafka(KafkaConsumer<String, String> consumer, String entityId, LocalDateTime deadline) {
    while (LocalDateTime.now().isBefore(deadline)) {
      long millis = ChronoUnit.MILLIS.between(LocalDateTime.now(), deadline);
      ConsumerRecords<String, String> records = consumer.poll(millis);
      if (!records.isEmpty()) {
        for (ConsumerRecord<String, String> record : records) {
          if (record.key().equals(entityId)) {
            return;
          }
        }
      }
    }
    throw new RuntimeException("entity not found: " + entityId);
  }
}
