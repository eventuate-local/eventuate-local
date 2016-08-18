package io.eventuate.local.cdc.debezium;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.zookeeper")
public class EventuateLocalZookeperConfigurationProperties {

  private String connectionString;

  public String getConnectionString() {
    return connectionString;
  }

  public void setConnectionString(String connectionString) {
    this.connectionString = connectionString;
  }
}
