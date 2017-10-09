package io.eventuate.local.cdc.debezium;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.cdc")
public class EventTableChangesToAggregateTopicRelayConfigurationProperties {

  private String dbUserName;

  private String dbPassword;

  private int pollingIntervalInMilliseconds = 500;

  private int maxEventsPerPolling = 1000;

  private int maxAttemptsForPolling = 100;

  private int pollingRetryIntervalInMilliseconds = 500;

  private int maxRetries = 5;
  private long retryPeriodInMilliseconds = 500;


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

  public int getPollingIntervalInMilliseconds() {
    return pollingIntervalInMilliseconds;
  }

  public void setPollingIntervalInMilliseconds(int pollingIntervalInMilliseconds) {
    this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
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

  public int getMaxEventsPerPolling() {
    return maxEventsPerPolling;
  }

  public void setMaxEventsPerPolling(int maxEventsPerPolling) {
    this.maxEventsPerPolling = maxEventsPerPolling;
  }

  public int getMaxAttemptsForPolling() {
    return maxAttemptsForPolling;
  }

  public void setMaxAttemptsForPolling(int maxAttemptsForPolling) {
    this.maxAttemptsForPolling = maxAttemptsForPolling;
  }

  public int getPollingRetryIntervalInMilliseconds() {
    return pollingRetryIntervalInMilliseconds;
  }

  public void setPollingRetryIntervalInMilliseconds(int pollingRetryIntervalInMilliseconds) {
    this.pollingRetryIntervalInMilliseconds = pollingRetryIntervalInMilliseconds;
  }
}
