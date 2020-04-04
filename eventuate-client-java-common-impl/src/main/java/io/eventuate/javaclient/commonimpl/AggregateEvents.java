package io.eventuate.javaclient.commonimpl;

import io.eventuate.SubscriberOptions;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface AggregateEvents {
  CompletableFuture<?> subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<SerializedEvent, CompletableFuture<?>> handler);
}
