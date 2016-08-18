package io.eventuate.local.java.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.kafka")
public class EventuateKafkaConfigurationProperties {

  private String bootstrapServers = "192.168.99.100:9092";

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public void setBootstrapServers(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

}
