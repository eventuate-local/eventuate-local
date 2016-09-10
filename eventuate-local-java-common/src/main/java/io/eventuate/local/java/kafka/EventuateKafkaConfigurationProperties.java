package io.eventuate.local.java.kafka;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@ConfigurationProperties("eventuateLocal.kafka")
public class EventuateKafkaConfigurationProperties {

  @NotBlank
  private String bootstrapServers;

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public void setBootstrapServers(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

}
