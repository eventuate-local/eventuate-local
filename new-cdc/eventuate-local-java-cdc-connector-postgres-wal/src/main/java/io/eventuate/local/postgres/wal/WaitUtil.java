package io.eventuate.local.postgres.wal;

public class WaitUtil {
  private boolean waiting = false;
  private long timeoutInMilliseconds;
  private long startTimeInMilliseconds;

  public WaitUtil(long timeoutInMilliseconds) {
    this.timeoutInMilliseconds = timeoutInMilliseconds;
  }

  public synchronized boolean isWaiting() {
    return waiting;
  }

  public synchronized boolean start() {
    if (!waiting) {
      startTimeInMilliseconds = System.currentTimeMillis();
      waiting = true;
      return true;
    }
    return false;
  }

  public synchronized boolean tick() {
    boolean continueWaiting = (System.currentTimeMillis() - startTimeInMilliseconds <= timeoutInMilliseconds);
    if (!continueWaiting) {
      stop();
    }
    return continueWaiting;
  }

  public synchronized void stop() {
    waiting = false;
  }
}