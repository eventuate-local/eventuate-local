package io.eventuate.local.mysql.binlog;

import io.eventuate.EntityIdAndType;
import io.eventuate.Int128;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.example.banking.domain.AccountDebitedEvent;
import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AbstractCdcTest {

  @Autowired
  Environment environment;

  @Autowired
  ApplicationContext applicationContext;

  public String generateAccountCreatedEvent() {
    return JSonMapper.toJson(new AccountCreatedEvent(new BigDecimal(System.currentTimeMillis())));
  }

  public String generateAccountDebitedEvent() {
    return JSonMapper.toJson(new AccountDebitedEvent(new BigDecimal(System.currentTimeMillis()), null));
  }

  public BinlogFileOffset generateBinlogFileOffset() {
    long now = System.currentTimeMillis();
    return new BinlogFileOffset("binlog.filename." + now, now);
  }

  public String generateUniqueTopicName() {
    return "test_topic_" + System.currentTimeMillis();
  }

  public String getEventTopicName() {
    return Account.class.getTypeName();
  }

  public EntityIdVersionAndEventIds saveEvent(EventuateLocalAggregateCrud localAggregateCrud, String eventData) {
    List<EventTypeAndData> events = Collections.singletonList(new EventTypeAndData(AccountCreatedEvent.class.getTypeName(), eventData, Optional.empty()));

    return localAggregateCrud.save(Account.class.getTypeName(), events, Optional.empty());
  }

  public EntityIdVersionAndEventIds updateEvent(String entityId, Int128 entityVersion, EventuateLocalAggregateCrud localAggregateCrud, String eventData) {
    List<EventTypeAndData> events = Collections.singletonList(new EventTypeAndData(AccountCreatedEvent.class.getTypeName(), eventData, Optional.empty()));

    return localAggregateCrud.update(new EntityIdAndType(entityId, Account.class.getTypeName()),
            entityVersion,
            events,
            Optional.empty());
  }

  public KafkaConsumer<String, String> createConsumer(String bootstrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("auto.offset.reset", "earliest");
    props.put("group.id", UUID.randomUUID().toString());
    props.put("enable.auto.commit", "false");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    return new KafkaConsumer<>(props);
  }

  public Producer<String, String> createProducer(String bootstrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    return new KafkaProducer<>(props);
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

  public void waitForEventInKafka(KafkaConsumer<String, String> consumer, String entityId, LocalDateTime deadline) throws InterruptedException {
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
