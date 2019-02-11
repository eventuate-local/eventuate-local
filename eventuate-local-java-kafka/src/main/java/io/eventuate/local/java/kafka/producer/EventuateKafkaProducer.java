package io.eventuate.local.java.kafka.producer;

import io.eventuate.local.java.common.broker.DataProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EventuateKafkaProducer implements DataProducer {

  private Producer<String, String> producer;
  private Properties producerProps;
  private boolean transaction = false;

  public EventuateKafkaProducer(String bootstrapServers,
                                EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {
    producerProps = new Properties();
    producerProps.put("bootstrap.servers", bootstrapServers);
    producerProps.put("acks", "all");
    producerProps.put("retries", 10);
    producerProps.put("batch.size", 16384);
    producerProps.put("linger.ms", 1);
    producerProps.put("buffer.memory", 33554432);
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producerProps.put("transactional.id", UUID.randomUUID().toString()); //TODO: made configurable as part of reader
    producerProps.putAll(eventuateKafkaProducerConfigurationProperties.getProperties());
    producer = new KafkaProducer<>(producerProps);
    producer.initTransactions();
  }

  @Override
  public CompletableFuture<?> send(String topic, String key, String body) {
    if (!transaction) {
      producer.beginTransaction();
    }

    CompletableFuture<Object> result = new CompletableFuture<>();
    producer.send(new ProducerRecord<>(topic, key, body), (metadata, exception) -> {
      if (exception == null)
        result.complete(metadata);
      else
        result.completeExceptionally(exception);
    });

    if (!transaction) {
      producer.commitTransaction();
    }

    return result;
  }

  public void beginTransaction() {
    producer.beginTransaction();
    transaction = true;
  }

  public void commitTransaction() {
    producer.commitTransaction();
    transaction = false;
  }

  public List<PartitionInfo> partitionsFor(String topic) {
    return producer.partitionsFor(topic);
  }

  public void close() {
    producer.close(1, TimeUnit.SECONDS);
  }
}
