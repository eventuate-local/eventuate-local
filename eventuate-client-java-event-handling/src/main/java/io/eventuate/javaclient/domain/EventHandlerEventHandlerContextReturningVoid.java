package io.eventuate.javaclient.domain;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import io.eventuate.EventuateAggregateStoreCrud;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

class EventHandlerEventHandlerContextReturningVoid implements EventHandler {
  private EventuateAggregateStoreCrud aggregateStore;
  private final Method method;
  private final Object eventHandler;

  public EventHandlerEventHandlerContextReturningVoid(EventuateAggregateStoreCrud aggregateStore, Method method, Object eventHandler) {
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
    CompletableFuture<Void> cf = new CompletableFuture<>();
    try {
      method.invoke(eventHandler, new EventHandlerContextImpl(aggregateStore, de));
      cf.complete(null);
    } catch (Throwable e) {
      cf.completeExceptionally(e);
    }
    return cf;
  }
}
