package io.eventuate.local.common;

import org.springframework.beans.factory.annotation.Value;

public class EventuateLocalZookeperConfigurationProperties {

  @Value("${eventuatelocal.zookeeper.connection.string}")
  private String connectionString;

  public String getConnectionString() {
    return connectionString;
  }
}
