package io.eventuate.local.mysql.binlog;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.polling.cdc")
public class PollingConfigurationProperties {

  private int pollingRequestPeriodInMilliseconds;

  private int maxEventsPerPolling;

  private int maxAttemptsForPolling;

  private int delayPerPollingAttemptInMilliseconds;

  public PollingConfigurationProperties() {
  }

  public PollingConfigurationProperties(int pollingRequestPeriodInMilliseconds, int maxEventsPerPolling, int maxAttemptsForPolling, int delayPerPollingAttemptInMilliseconds) {
    this.pollingRequestPeriodInMilliseconds = pollingRequestPeriodInMilliseconds;
    this.maxEventsPerPolling = maxEventsPerPolling;
    this.maxAttemptsForPolling = maxAttemptsForPolling;
    this.delayPerPollingAttemptInMilliseconds = delayPerPollingAttemptInMilliseconds;
  }

  public int getPollingRequestPeriodInMilliseconds() {
    return pollingRequestPeriodInMilliseconds;
  }

  public void setPollingRequestPeriodInMilliseconds(int pollingRequestPeriodInMilliseconds) {
    this.pollingRequestPeriodInMilliseconds = pollingRequestPeriodInMilliseconds;
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

  public int getDelayPerPollingAttemptInMilliseconds() {
    return delayPerPollingAttemptInMilliseconds;
  }

  public void setDelayPerPollingAttemptInMilliseconds(int delayPerPollingAttemptInMilliseconds) {
    this.delayPerPollingAttemptInMilliseconds = delayPerPollingAttemptInMilliseconds;
  }
}
