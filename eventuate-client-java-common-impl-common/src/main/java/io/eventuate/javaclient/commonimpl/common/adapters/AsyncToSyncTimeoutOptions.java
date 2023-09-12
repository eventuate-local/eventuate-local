package io.eventuate.javaclient.commonimpl.common.adapters;

import java.util.concurrent.TimeUnit;

public class AsyncToSyncTimeoutOptions {

  private long timeout = 30;
  private TimeUnit timeUnit = TimeUnit.SECONDS;

  public long getTimeout() {
    return timeout;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }
}
