package io.eventuate;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CompletableFutureTest {

  @Test
  public void exploreHandleAsync() throws ExecutionException, InterruptedException {
    CompletableFuture<Void> f = new CompletableFuture<>();
    CompletableFuture<Void> f2 = f.handleAsync((r, t) -> {
      throw new RuntimeException("y", t);
    });
    CompletableFuture<Void> f3 = f2.handleAsync((r, t) -> {
      throw new RuntimeException("z", t);
    });
    f.completeExceptionally(new RuntimeException("x"));
    try {
      f3.get();
      fail();
    } catch (ExecutionException e ) {
      e.printStackTrace();
      assertTrue(e.getCause() instanceof RuntimeException);
      assertEquals("z", e.getCause().getMessage());
    }
  }
}
