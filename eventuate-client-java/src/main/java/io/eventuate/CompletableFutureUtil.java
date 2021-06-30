package io.eventuate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class CompletableFutureUtil {
  public static Throwable unwrap(Throwable throwable) {
    if (throwable instanceof ExecutionException)
      return throwable.getCause();
    else if (throwable instanceof CompletionException)
      return throwable.getCause();
    else
      return throwable;
  }

  public static <T> CompletableFuture<T> failedFuture(Throwable t) {
    CompletableFuture<T> f = new CompletableFuture<>();
    f.completeExceptionally(t);
    return f;
  }

  public static <T> CompletableFuture<T> tap(CompletableFuture<T> input, BiConsumer<T, Throwable> tapper) {
    CompletableFuture<T> outcome = new CompletableFuture<>();
    input.handle((result, throwable) -> {
      tapper.accept(result, throwable);
      completeSomehow(outcome, result, throwable);
      return null;
    });
    return outcome;
  }

  public static <T> void completeSomehow(CompletableFuture<T> outcome, T result, Throwable throwable) {
    if (throwable == null)
      outcome.complete(result);
    else
      outcome.completeExceptionally(throwable);
  }

}
