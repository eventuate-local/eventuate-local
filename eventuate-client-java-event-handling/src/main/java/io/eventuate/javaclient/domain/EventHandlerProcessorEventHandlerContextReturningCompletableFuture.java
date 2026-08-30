package io.eventuate.javaclient.domain;

import io.eventuate.*;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class EventHandlerProcessorEventHandlerContextReturningCompletableFuture extends EventHandlerMethodProcessor {

  private EventuateAggregateStoreCrud aggregateStore;

  public EventHandlerProcessorEventHandlerContextReturningCompletableFuture(EventuateAggregateStoreCrud aggregateStore) {
    this.aggregateStore = aggregateStore;
  }

  @Override
  public boolean supportsMethod(Method method) {
    return EventHandlerProcessorUtil.isMethodWithOneParameterOfTypeReturning(method, EventHandlerContext.class, CompletableFuture.class);
  }

  @Override
  public EventHandler processMethod(Object eventHandler, Method method) {
    return new EventHandlerContextReturningCompletableFuture(aggregateStore, method, eventHandler);
  }

}
