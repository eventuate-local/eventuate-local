package io.eventuate.local.java.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EventuateKafkaProducer {

  private Producer<String, String> producer;
  private String bootstrapServers;
  private Properties producerProps;

  public EventuateKafkaProducer(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
    producerProps = new Properties();
    producerProps.put("bootstrap.servers", bootstrapServers);
    producerProps.put("acks", "all");
    producerProps.put("retries", 0);
    producerProps.put("batch.size", 16384);
    producerProps.put("linger.ms", 1);
    producerProps.put("buffer.memory", 33554432);
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producer = new KafkaProducer<>(producerProps);
  }


  public CompletableFuture<?> send(String topic, String key, String body) {
    CompletableFuture<Object> result = new CompletableFuture<>();
    producer.send(new ProducerRecord<>(topic, key, body), (metadata, exception) -> {
      if (exception == null)
        result.complete(metadata);
      else
        result.completeExceptionally(exception);
    });
    return result;
  }

  public void close() {
    producer.close(1, TimeUnit.SECONDS);
  }
}
