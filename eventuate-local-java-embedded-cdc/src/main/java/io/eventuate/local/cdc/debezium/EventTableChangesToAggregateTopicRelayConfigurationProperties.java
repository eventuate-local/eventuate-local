package io.eventuate.local.cdc.debezium;

import org.springframework.beans.factory.annotation.Value;

public class EventTableChangesToAggregateTopicRelayConfigurationProperties {

  @Value("${eventuatelocal.cdc.db.user.name:#{null}}")
  private String dbUserName;

  @Value("${eventuatelocal.cdc.db.password:#{null}}")
  private String dbPassword;

  @Value("${eventuatelocal.cdc.polling.interval.in.milliseconds:#{500}}")
  private int pollingIntervalInMilliseconds;

  @Value("${eventuatelocal.cdc.max.events.per.polling:#{1000}}")
  private int maxEventsPerPolling;

  @Value("${eventuatelocal.cdc.max.attempts.for.polling:#{100}}")
  private int maxAttemptsForPolling;

  @Value("${eventuatelocal.cdc.polling.retry.interval.in.milliseconds:#{500}}")
  private int pollingRetryIntervalInMilliseconds;

  @Value("${eventuatelocal.cdc.max.retries:#{5}}")
  private int maxRetries;

  @Value("${eventuatelocal.cdc.retry.period.in.milliseconds:#{500}}")
  private long retryPeriodInMilliseconds;

  @Value("${eventuatelocal.cdc.leadership.lock.path:#{\"/eventuatelocal/cdc/leader\"}}")
  private String leadershipLockPath;

  public String getDbUserName() {
    return dbUserName;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public int getPollingIntervalInMilliseconds() {
    return pollingIntervalInMilliseconds;
  }

  public int getMaxEventsPerPolling() {
    return maxEventsPerPolling;
  }

  public int getMaxAttemptsForPolling() {
    return maxAttemptsForPolling;
  }

  public int getPollingRetryIntervalInMilliseconds() {
    return pollingRetryIntervalInMilliseconds;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public long getRetryPeriodInMilliseconds() {
    return retryPeriodInMilliseconds;
  }

  public String getLeadershipLockPath() {
    return leadershipLockPath;
  }
}
