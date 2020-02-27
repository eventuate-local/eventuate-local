package io.eventuate.local.java.jdbc.jdbckafkastore;


import io.eventuate.SubscriberOptions;
import io.eventuate.common.eventuate.local.PublishedEvent;
import io.eventuate.common.id.Int128;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.javaclient.commonimpl.AggregateEvents;
import io.eventuate.javaclient.commonimpl.SerializedEvent;
import io.eventuate.messaging.kafka.basic.consumer.EventuateKafkaConsumer;
import io.eventuate.messaging.kafka.basic.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.messaging.kafka.common.AggregateTopicMapping;
import io.eventuate.messaging.kafka.common.EventuateKafkaConfigurationProperties;
import io.eventuate.messaging.kafka.common.EventuateKafkaMultiMessageConverter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Subscribe to domain events published to Kafka
 */
public class EventuateKafkaAggregateSubscriptions implements AggregateEvents {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration;
  private EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;
  private EventuateKafkaMultiMessageConverter eventuateKafkaMultiMessageConverter = new EventuateKafkaMultiMessageConverter();

  public EventuateKafkaAggregateSubscriptions(EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration,
                                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    this.eventuateLocalAggregateStoreConfiguration = eventuateLocalAggregateStoreConfiguration;
    this.eventuateKafkaConsumerConfigurationProperties = eventuateKafkaConsumerConfigurationProperties;
  }

  private final List<EventuateKafkaConsumer> consumers = new ArrayList<>();

  @PreDestroy
  public void cleanUp() {
    synchronized (consumers) {
      consumers.stream().forEach(EventuateKafkaConsumer::stop);
    }
    logger.debug("Waiting for consumers to commit");
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      logger.error("Error waiting", e);
    }
  }

  private void addConsumer(EventuateKafkaConsumer consumer) {
    synchronized (consumers) {
      consumers.add(consumer);
    }
  }

  @Override
  public CompletableFuture<?> subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents,
                                        SubscriberOptions subscriberOptions,
                                        Function<SerializedEvent, CompletableFuture<?>> handler) {

    logger.info("Subscribing: subscriberId = {}, aggregatesAndEvents = {}, options = {}", subscriberId, aggregatesAndEvents, subscriberOptions);

    List<String> topics = aggregatesAndEvents.keySet()
            .stream()
            .map(AggregateTopicMapping::aggregateTypeToTopic)
            .collect(toList());

    logger.info("Creating consumer: subscriberId = {}, aggregatesAndEvents = {}, options = {}", subscriberId, aggregatesAndEvents, subscriberOptions);

    EventuateKafkaConsumer consumer = new EventuateKafkaConsumer(subscriberId, (record, callback) -> {
      List<SerializedEvent> serializedEvents = toSerializedEvents(record);

      for (SerializedEvent se : serializedEvents) {
        if (aggregatesAndEvents.get(se.getEntityType()).contains(se.getEventType())) {
          handler.apply(se).whenComplete((result, t) -> {
            callback.accept(null, t);
          });
        } else {
          callback.accept(null, null);
        }
      }

      return null;
    }, topics, eventuateLocalAggregateStoreConfiguration.getBootstrapServers(), eventuateKafkaConsumerConfigurationProperties);

    addConsumer(consumer);

    logger.info("Starting consumer: subscriberId = {}, aggregatesAndEvents = {}, options = {}", subscriberId, aggregatesAndEvents, subscriberOptions);

    consumer.start();

    logger.info("Subscribed: subscriberId = {}, aggregatesAndEvents = {}, options = {}", subscriberId, aggregatesAndEvents, subscriberOptions);

    return CompletableFuture.completedFuture(null);
  }

  private List<SerializedEvent> toSerializedEvents(ConsumerRecord<String, byte[]> record) {
    return eventuateKafkaMultiMessageConverter.convertBytesToValues(record.value())
            .stream()
            .map(value -> jsonToSerializedEvent(value, record))
            .collect(Collectors.toList());
  }

  private SerializedEvent jsonToSerializedEvent(String value, ConsumerRecord<String, byte[]> record) {
    PublishedEvent pe = JSonMapper.fromJson(value, PublishedEvent.class);
    return new SerializedEvent(
            Int128.fromString(pe.getId()),
            pe.getEntityId(),
            pe.getEntityType(),
            pe.getEventData(),
            pe.getEventType(),
            record.partition(),
            record.offset(),
            EtopEventContext.make(pe.getId(), record.topic(), record.partition(), record.offset()),
            pe.getMetadata());
  }

}
