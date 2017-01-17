package io.eventuate.local.java.kafka;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.kafka")
public class EventuateKafkaConfigurationProperties {

  @NotBlank
  private String bootstrapServers;

  private long connectionValidationTimeout=1000;

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public void setBootstrapServers(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  public long getConnectionValidationTimeout() {
    return connectionValidationTimeout;
  }

  public void setConnectionValidationTimeout(long connectionValidationTimeout) {
    this.connectionValidationTimeout = connectionValidationTimeout;
  }
}
