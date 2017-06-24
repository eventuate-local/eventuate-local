package io.eventuate.local.cdc.debezium;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class TakeLeadershipAttemptTracker {
  private final int maxAttempts;
  private final long retryPeriod;
  private List<LocalDateTime> lastAttemptTimes = new LinkedList<>();

  public TakeLeadershipAttemptTracker(int maxAttempts, long retryPeriodInMilliseconds) {
    this.maxAttempts = maxAttempts;
    this.retryPeriod = retryPeriodInMilliseconds;
  }

  public void attempting() {
    lastAttemptTimes.add(now());
    lastAttemptTimes = lastAttemptTimes.subList(0, Math.min(lastAttemptTimes.size(), 30));
  }

  protected LocalDateTime now() {
    return LocalDateTime.now();
  }

  boolean shouldAttempt() {
    int attempts = 0;
    LocalDateTime now = now();
    for (LocalDateTime attempt : lastAttemptTimes) {
      long age = Duration.between(attempt, now).toMillis();
      if (age < retryPeriod)
        attempts++;
      else
        break;
    }
    return attempts <= maxAttempts;
  }

}