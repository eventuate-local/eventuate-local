package io.eventuate.javaclient.commonimpl.adapters;

import io.eventuate.CompletableFutureUtil;
import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.SerializedEvent;
import io.eventuate.javaclient.commonimpl.sync.AggregateEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class SyncToAsyncAggregateEventsAdapter implements io.eventuate.javaclient.commonimpl.AggregateEvents {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private io.eventuate.javaclient.commonimpl.sync.AggregateEvents target;

  public SyncToAsyncAggregateEventsAdapter(AggregateEvents target) {
    this.target = target;
  }

  @Override
  public CompletableFuture<?> subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<SerializedEvent, CompletableFuture<?>> handler) {
    try {
      logger.info("Subscribing: subscriberId = {}, aggregatesAndEvents = {}, options = {}", subscriberId, aggregatesAndEvents, subscriberOptions);
      target.subscribe(subscriberId, aggregatesAndEvents, subscriberOptions, handler);
      logger.info("Subscribed: subscriberId = {}, aggregatesAndEvents = {}, options = {}", subscriberId, aggregatesAndEvents, subscriberOptions);
      return CompletableFuture.completedFuture(null);
    } catch (RuntimeException e) {
      logger.error("Subscription failed", e);
      return CompletableFutureUtil.failedFuture(e);
    }
  }
}
