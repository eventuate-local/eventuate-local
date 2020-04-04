package io.eventuate.javaclient.domain;

import io.eventuate.DispatchedEvent;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class EventHandlerProcessorDispatchedEventReturningCompletableFuture extends EventHandlerMethodProcessor {

  @Override
  public boolean supportsMethod(Method method) {
    return EventHandlerProcessorUtil.isMethodWithOneParameterOfTypeReturning(method, DispatchedEvent.class, CompletableFuture.class);
  }

  @Override
  public EventHandler processMethod(Object eventHandler, Method method) {
    return new EventHandlerDispatchedEventReturningCompletableFuture(method, eventHandler);
  }


}
