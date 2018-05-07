package io.eventuate.local.java.kafka.producer;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("eventuate.local.kafka.producer")
public class EventuateKafkaProducerConfigurationProperties {
  Map<String, String> properties = new HashMap<>();

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public static EventuateKafkaProducerConfigurationProperties empty() {
    return new EventuateKafkaProducerConfigurationProperties();
  }
}
