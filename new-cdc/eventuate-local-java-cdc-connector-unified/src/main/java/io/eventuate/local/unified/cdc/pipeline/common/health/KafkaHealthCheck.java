package io.eventuate.local.unified.cdc.pipeline.common.health;

import io.eventuate.local.java.kafka.consumer.ConsumerPropertiesFactory;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class KafkaHealthCheck extends AbstractHealthCheck {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${eventuatelocal.kafka.bootstrap.servers}")
  private String kafkaServers;

  @Override
  public Health health() {

    List<String> errors;

    Properties consumerProperties = ConsumerPropertiesFactory.makeDefaultConsumerProperties(kafkaServers, UUID.randomUUID().toString());
    consumerProperties.put("session.timeout.ms", "500");
    consumerProperties.put("request.timeout.ms", "1000");
    consumerProperties.put("heartbeat.interval.ms", "100");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);

    try {
      consumer.partitionsFor("__consumer_offsets");
      errors = Collections.emptyList();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      errors = Collections.singletonList("Connection to kafka failed");
    } finally {
      try {
        consumer.close();
      } catch (Exception ce) {
        logger.error(ce.getMessage(), ce);
      }
    }

    return makeHealthFromErrors(errors);
  }
}
