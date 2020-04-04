package io.eventuate.javaclient.domain;

import io.eventuate.EventHandlerContext;
import io.eventuate.EventuateAggregateStore;

import java.lang.reflect.Method;

public class EventHandlerProcessorEventHandlerContextReturningVoid extends EventHandlerMethodProcessor {

  private EventuateAggregateStore aggregateStore;

  public EventHandlerProcessorEventHandlerContextReturningVoid(EventuateAggregateStore aggregateStore) {
    this.aggregateStore = aggregateStore;
  }

  @Override
  public boolean supportsMethod(Method method) {
    return EventHandlerProcessorUtil.isVoidMethodWithOneParameterOfType(method, EventHandlerContext.class);
  }

  @Override
  public EventHandler processMethod(Object eventHandler, Method method) {
    return new EventHandlerEventHandlerContextReturningVoid(aggregateStore, method, eventHandler);
  }


}
