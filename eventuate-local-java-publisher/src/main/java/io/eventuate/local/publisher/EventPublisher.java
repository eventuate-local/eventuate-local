package io.eventuate.local.publisher;

import io.eventuate.local.common.AggregateTopicMapping;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumer;
import io.eventuate.local.publisher.changes.EventInfo;
import io.eventuate.local.publisher.changes.EventRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.Properties;

/**
 * Consumes changes to the events table from Debezium and publishes messages to the aggregate topics
 */
public class EventPublisher {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventPublisherConfigurationProperties eventPublisherConfigurationProperties;
  private EventuateKafkaConsumer consumer;
  private Producer<String, String> producer;
  private String topic;
  private Properties producerProps;

  public EventPublisher(EventPublisherConfigurationProperties eventPublisherConfigurationProperties) {
    this.eventPublisherConfigurationProperties = eventPublisherConfigurationProperties;
    topic = eventPublisherConfigurationProperties.getEventTopic();

    producerProps = new Properties();
    producerProps.put("bootstrap.servers", eventPublisherConfigurationProperties.getBootstrapServers());
    producerProps.put("acks", "all");
    producerProps.put("retries", 0);
    producerProps.put("batch.size", 16384);
    producerProps.put("linger.ms", 1);
    producerProps.put("buffer.memory", 33554432);
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");


  }

  @PostConstruct
  public void start() throws Exception {

    producer = new KafkaProducer<>(producerProps);
    consumer = new EventuateKafkaConsumer(eventPublisherConfigurationProperties.getGroupId(),
            (record, doneCallback) -> {
              publish(EventRecordDeserializer.toEventRecord(record.value()));
              doneCallback.accept(null, null);
            },
            Collections.singletonList(topic),
            eventPublisherConfigurationProperties.getBootstrapServers());

    consumer.start();

  }

  @PreDestroy
  public void stop() {
    if (consumer != null)
      consumer.stop();
  }

  private void publish(EventRecord eventRecord) {

    EventInfo eventInfo = eventRecord.getPayload().getAfter();
    logger.info("Publishing {}", eventInfo);
    producer.send(new ProducerRecord<>(
            AggregateTopicMapping.aggregateTypeToTopic(eventInfo.getEntity_type()),
            eventInfo.getEntity_id(),
            EventRecordDeserializer.toJson(toPublishedEvent(eventInfo))
    ));

  }

  private PublishedEvent toPublishedEvent(EventInfo eventInfo) {
    return new PublishedEvent(eventInfo.getEvent_id(),
            eventInfo.getEntity_id(), eventInfo.getEntity_type(),
            eventInfo.getEvent_data(),
            eventInfo.getEvent_type());
  }

}
