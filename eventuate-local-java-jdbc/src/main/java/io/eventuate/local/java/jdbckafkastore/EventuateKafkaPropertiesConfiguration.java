package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateKafkaPropertiesConfiguration {
  @Bean
  public EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties() {
    return new EventuateKafkaConfigurationProperties();
  }
}
