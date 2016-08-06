package io.eventuate.local.publisher;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(EventPublisherConfigurationProperties.class)
public class EventuateLocalPublisherConfiguration {

  @Bean
  public EventPublisher eventPublisher(EventPublisherConfigurationProperties eventPublisherConfigurationProperties) {
    return new EventPublisher(eventPublisherConfigurationProperties);
  }
}
