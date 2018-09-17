package io.eventuate.local.cdc.debezium;

import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CdcStartupValidator {

  private long kafkaValidationTimeoutMillis;
  private int kafkaValidationMaxAttempts;

  private String bootstrapServers;

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;

  public CdcStartupValidator(String bootstrapServers,
                             EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    this.bootstrapServers = bootstrapServers;
    this.eventuateKafkaConsumerConfigurationProperties = eventuateKafkaConsumerConfigurationProperties;
  }

  public void validateEnvironment() {
    validateKafkaConnection();
  }

  private void validateKafkaConnection() {
    KafkaConsumer<String, String>  consumer = getTestConsumer();

    int i = kafkaValidationMaxAttempts;
    KafkaException lastException = null;
    while (i > 0) {
      try {
        consumer.listTopics();
        logger.info("Successfully tested Kafka connection");
        return;
      } catch (KafkaException e) {
        logger.info("Failed to connection to Kafka");
        lastException = e;
        i--;
        try {
          Thread.sleep(kafkaValidationTimeoutMillis);
        } catch (InterruptedException ie) {
          throw new RuntimeException("Kafka validation had been interrupted!", ie);
        }
      }
    }
    throw lastException;
  }

  private KafkaConsumer<String, String> getTestConsumer() {
    Properties consumerProperties = new Properties();
    consumerProperties.put("bootstrap.servers", bootstrapServers);
    consumerProperties.put("group.id", "stratup-test-subscriber");
    consumerProperties.put("request.timeout.ms", String.valueOf(kafkaValidationTimeoutMillis + 1));
    consumerProperties.put("session.timeout.ms", String.valueOf(kafkaValidationTimeoutMillis));
    consumerProperties.put("heartbeat.interval.ms", String.valueOf(kafkaValidationTimeoutMillis - 1));
    consumerProperties.put("fetch.max.wait.ms", String.valueOf(kafkaValidationTimeoutMillis));
    consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    consumerProperties.putAll(eventuateKafkaConsumerConfigurationProperties.getProperties());

    return new KafkaConsumer<>(consumerProperties);
  }

  public void setKafkaValidationTimeoutMillis(long kafkaValidationTimeoutMillis) {
    this.kafkaValidationTimeoutMillis = kafkaValidationTimeoutMillis;
  }

  public void setKafkaValidationMaxAttempts(int kafkaValidationMaxAttempts) {
    this.kafkaValidationMaxAttempts = kafkaValidationMaxAttempts;
  }
}
