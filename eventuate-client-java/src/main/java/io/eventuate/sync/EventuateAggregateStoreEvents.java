package io.eventuate.sync;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import io.eventuate.SubscriberOptions;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface EventuateAggregateStoreEvents {
  void subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<DispatchedEvent<Event>, CompletableFuture<?>> dispatch);
}
