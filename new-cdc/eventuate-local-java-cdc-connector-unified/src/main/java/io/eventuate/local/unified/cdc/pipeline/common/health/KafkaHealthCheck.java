package io.eventuate.local.unified.cdc.pipeline.common.health;

import io.eventuate.local.java.kafka.consumer.ConsumerPropertiesFactory;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import java.util.Properties;
import java.util.UUID;

public class KafkaHealthCheck extends AbstractHealthCheck {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${eventuatelocal.kafka.bootstrap.servers}")
  private String kafkaServers;

  private KafkaConsumer<String, String> consumer;

  @Override
  protected void determineHealth(HealthBuilder builder) {

    if (consumer == null) {
      Properties consumerProperties = ConsumerPropertiesFactory.makeDefaultConsumerProperties(kafkaServers, UUID.randomUUID().toString());
      consumerProperties.put("session.timeout.ms", "500");
      consumerProperties.put("request.timeout.ms", "1000");
      consumerProperties.put("heartbeat.interval.ms", "100");
      consumer = new KafkaConsumer<>(consumerProperties);
    }

    try {
      consumer.partitionsFor("__consumer_offsets");
      builder.addDetail("Connected to Kafka");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      builder.addError("Connection to kafka failed");
    }
  }
}
