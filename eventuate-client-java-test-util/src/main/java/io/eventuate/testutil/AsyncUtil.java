package io.eventuate.testutil;

import io.eventuate.CompletableFutureUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncUtil {
  public static <T> T await(CompletableFuture<T> future) {
    try {
      return future.get(30, TimeUnit.SECONDS);
    } catch (InterruptedException | TimeoutException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      Throwable unwrapped = CompletableFutureUtil.unwrap(e);
      if (unwrapped instanceof RuntimeException)
        throw (RuntimeException)unwrapped;
      else
        throw new RuntimeException(unwrapped);
    }
  }
}
