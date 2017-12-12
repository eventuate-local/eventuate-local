package io.eventuate.local.java.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class EventuateKafkaConfigurationProperties {

  @Value("${eventuatelocal.kafka.bootstrap.servers}")
  private String bootstrapServers;

  @Value("${eventuatelocal.kafka.connection.validation.timeout:#{1000}}")
  private long connectionValidationTimeout;

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
