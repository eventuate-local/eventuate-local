package io.eventuate.local.java.kafka;

import org.springframework.beans.factory.annotation.Value;

public class EventuateKafkaConfigurationProperties {

  @Value("${eventuatelocal.kafka.bootstrap.servers}")
  private String bootstrapServers;

  @Value("${eventuatelocal.kafka.connection.validation.timeout:#{1000}}")
  private long connectionValidationTimeout;

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public long getConnectionValidationTimeout() {
    return connectionValidationTimeout;
  }
}
