package io.eventuate.local.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class EventuateLocalZookeperConfigurationProperties {

  @Value("${eventuatelocal.zookeeper.connection.string}")
  private String connectionString;

  public String getConnectionString() {
    return connectionString;
  }

  public void setConnectionString(String connectionString) {
    this.connectionString = connectionString;
  }
}
