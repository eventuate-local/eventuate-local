package io.eventuate.local.java.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateKafkaPropertiesConfiguration {
  @Bean
  public EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties() {
    return new EventuateKafkaConfigurationProperties();
  }
}
