package io.eventuate.testutil;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Eventually {

  public static void eventually(Runnable body) {
    eventually(10, 500, TimeUnit.MILLISECONDS, body);
  }

  public static void eventually(int iterations, int timeout, TimeUnit timeUnit, Runnable body) {
    eventuallyReturning(iterations, timeout, timeUnit, () -> { body.run(); ; return null; });
  }

  public static <T>  T eventuallyReturning(int iterations, int timeout, TimeUnit timeUnit, Supplier<T> body) {
    Throwable t = null;
    for (int i = 0; i < iterations; i++) {
      try {
        return body.get();
      } catch (Throwable t1) {
        t = t1;
        try {
          timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
    throw new EventuallyException(String.format("Failed after %s iterations every %s %s", iterations, timeout, timeUnit), t);
  }
}
