package io.eventuate.javaclient.commonimpl.sync;

import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.SerializedEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface AggregateEvents {
  void subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<SerializedEvent, CompletableFuture<?>> handler);
}
