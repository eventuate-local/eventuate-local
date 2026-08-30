package io.eventuate.javaclient.domain;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;

import java.util.concurrent.CompletableFuture;

public interface EventHandler {
  Class<Event> getEventType();

  CompletableFuture<?> dispatch(DispatchedEvent<Event> de);
}
