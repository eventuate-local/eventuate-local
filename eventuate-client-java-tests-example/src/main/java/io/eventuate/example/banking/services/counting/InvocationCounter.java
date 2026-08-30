package io.eventuate.example.banking.services.counting;

import java.util.concurrent.atomic.AtomicInteger;

public class InvocationCounter {
  private AtomicInteger counter = new AtomicInteger(0);

  public void increment() {
    counter.incrementAndGet();
  }

  public int get() {
    return counter.get();
  }
}
