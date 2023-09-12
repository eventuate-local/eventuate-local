package io.eventuate.javaclient.domain;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

class EventHandlerDispatchedEventReturningVoid implements EventHandler {
  private final Method method;
  private final Object eventHandler;

  public EventHandlerDispatchedEventReturningVoid(Method method, Object eventHandler) {
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
      method.invoke(eventHandler, de);
      cf.complete(null);
    } catch (Throwable e) {
      cf.completeExceptionally(e);
    }
    return cf;
  }

}
