package io.eventuate.javaclient.domain;

import io.eventuate.*;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

class EventHandlerContextReturningCompletableFuture implements EventHandler {
  private final Method method;
  private final Object eventHandler;
  private EventuateAggregateStoreCrud aggregateStore;

  public EventHandlerContextReturningCompletableFuture(EventuateAggregateStoreCrud aggregateStore, Method method, Object eventHandler) {
    this.aggregateStore = aggregateStore;
    this.method = method;
    this.eventHandler = eventHandler;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("method", method.getName()).toString();
  }

  @Override
  public Class<Event> getEventType() {
    return EventHandlerProcessorUtil.getEventClass(method);
  }

  @Override
  public CompletableFuture<?> dispatch(DispatchedEvent<Event> de) {
    try {
      return (CompletableFuture<?>) method.invoke(eventHandler, new EventHandlerContextImpl(aggregateStore, de));
    } catch (Throwable e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }
}
