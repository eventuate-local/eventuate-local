package io.eventuate.local.cdc.debezium;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@ConfigurationProperties("eventuateLocal.cdc")
public class EventTableChangesToAggregateTopicRelayConfigurationProperties {

  @NotBlank
  private String dbUserName;

  @NotBlank
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
