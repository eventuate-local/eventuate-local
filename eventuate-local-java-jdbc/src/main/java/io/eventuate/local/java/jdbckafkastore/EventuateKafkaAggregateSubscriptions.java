package io.eventuate.local.java.jdbckafkastore;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.EventContext;
import io.eventuate.Int128;
import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.AggregateEvents;
import io.eventuate.javaclient.commonimpl.SerializedEvent;
import io.eventuate.local.common.AggregateTopicMapping;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * Subscribe to domain events published to Kafka
 */
public class EventuateKafkaAggregateSubscriptions implements AggregateEvents {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration;

  public EventuateKafkaAggregateSubscriptions(EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration) {
    this.eventuateLocalAggregateStoreConfiguration = eventuateLocalAggregateStoreConfiguration;
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

    List<String> topics = aggregatesAndEvents.keySet()
            .stream()
            .map(AggregateTopicMapping::aggregateTypeToTopic)
            .collect(toList());

    EventuateKafkaConsumer consumer = new EventuateKafkaConsumer(subscriberId, (record, callback) -> {
      SerializedEvent se = toSerializedEvent(record);
      if (aggregatesAndEvents.get(se.getEntityType()).contains(se.getEventType())) {
        handler.apply(se).whenComplete((result, t) -> {
          callback.accept(null, t);
        });
      } else {
        callback.accept(null, null);
      }

    }, topics, eventuateLocalAggregateStoreConfiguration.getBootstrapServers());

    addConsumer(consumer);
    consumer.start();

    return CompletableFuture.completedFuture(null);

  }

  private SerializedEvent toSerializedEvent(ConsumerRecord<String, String> record) {
    try {
      ObjectMapper om = new ObjectMapper();
      PublishedEvent pe = om.readValue(record.value(), PublishedEvent.class);
      return new SerializedEvent(
              Int128.fromString(pe.getId()),
              pe.getEntityId(),
              pe.getEntityType(),
              pe.getEventData(),
              pe.getEventType(),
              record.partition(),
              record.offset(),
              new EventContext(String.format("eto:%s:%s:%s", pe.getId(), record.topic(), record.offset())));
    } catch (IOException e) {
      logger.error("Got exception: ", e);
      throw new RuntimeException(e);
    }
  }


}
