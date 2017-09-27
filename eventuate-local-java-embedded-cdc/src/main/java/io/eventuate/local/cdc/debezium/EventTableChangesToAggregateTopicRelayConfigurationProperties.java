package io.eventuate.local.cdc.debezium;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@ConfigurationProperties("eventuateLocal.cdc")
public class EventTableChangesToAggregateTopicRelayConfigurationProperties {

  private String dbUserName;

  private String dbPassword;

  private int pollingRequestPeriodInMilliseconds;

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

  public int getPollingRequestPeriodInMilliseconds() {
    return pollingRequestPeriodInMilliseconds;
  }

  public void setPollingRequestPeriodInMilliseconds(int pollingRequestPeriodInMilliseconds) {
    this.pollingRequestPeriodInMilliseconds = pollingRequestPeriodInMilliseconds;
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
