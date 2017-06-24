package io.eventuate.local.cdc.debezium;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.cdc")
public class EventTableChangesToAggregateTopicRelayConfigurationProperties {

  @NotBlank
  private String dbUserName;

  @NotBlank
  private String dbPassword;

  private int maxRetries = 5;
  private long retryPeriodInMilliseconds = 60 * 1000;

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

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public void setRetryPeriodInMilliseconds(long retryPeriodInMilliseconds) {
    this.retryPeriodInMilliseconds = retryPeriodInMilliseconds;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public long getRetryPeriodInMilliseconds() {
    return retryPeriodInMilliseconds;
  }
}
