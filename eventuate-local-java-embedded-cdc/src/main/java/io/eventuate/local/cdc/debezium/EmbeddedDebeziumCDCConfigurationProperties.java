package io.eventuate.local.cdc.debezium;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.embedded.cdc")
public class EmbeddedDebeziumCDCConfigurationProperties {

  private String dbUserName;

  private String dbPassword;

  public String getDbUserName() {
    return dbUserName;
  }

  public void setDbUserName(String dbUserName) {
    this.dbUserName = dbUserName;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public void setDbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }
}
