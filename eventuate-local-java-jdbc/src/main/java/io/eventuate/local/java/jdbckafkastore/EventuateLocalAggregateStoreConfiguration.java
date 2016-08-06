package io.eventuate.local.java.jdbckafkastore;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.aggregateStore")
public class EventuateLocalAggregateStoreConfiguration {

  private String bootstrapServers = "192.168.99.100:9092";

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public void setBootstrapServers(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

}
