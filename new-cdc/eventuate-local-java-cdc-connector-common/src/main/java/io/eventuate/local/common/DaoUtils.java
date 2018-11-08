package io.eventuate.local.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class DaoUtils {
  private static Logger logger = LoggerFactory.getLogger(DaoUtils.class);

  public static <T> T handleConnectionLost(int maxAttempts,
                                           int intervalInMilliseconds,
                                           Callable<T> query,
                                           Runnable onInterruptedCallback) {
    int attempt = 0;

    while(true) {
      try {
        T result = query.call();
        if (attempt > 0)
          logger.info("Reconnected to database");
        return result;
      } catch (Exception e) {

        logger.error(String.format("Could not access database %s - retrying in %s milliseconds", e.getMessage(), intervalInMilliseconds), e);

        if (attempt++ >= maxAttempts) {
          throw new RuntimeException(e);
        }

        try {
          Thread.sleep(intervalInMilliseconds);
        } catch (InterruptedException ie) {
          onInterruptedCallback.run();
          throw new RuntimeException(ie);
        }
      }
    }
  }
}
