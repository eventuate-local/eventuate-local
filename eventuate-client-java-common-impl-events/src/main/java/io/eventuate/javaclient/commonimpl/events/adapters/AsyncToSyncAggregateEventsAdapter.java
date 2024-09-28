package io.eventuate.javaclient.commonimpl.events.adapters;

import io.eventuate.CompletableFutureUtil;
import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.events.AggregateEvents;
import io.eventuate.javaclient.commonimpl.common.SerializedEvent;
import io.eventuate.javaclient.commonimpl.common.adapters.AsyncToSyncTimeoutOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AsyncToSyncAggregateEventsAdapter implements io.eventuate.javaclient.commonimpl.events.sync.AggregateEvents {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private AggregateEvents target;
  private AsyncToSyncTimeoutOptions timeoutOptions = new AsyncToSyncTimeoutOptions();

  public AsyncToSyncAggregateEventsAdapter(AggregateEvents target) {
    this.target = target;
  }

  @Override
  public void subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<SerializedEvent, CompletableFuture<?>> handler) {
    logger.info("Subscribing: subscriberId = {}, aggregatesAndEvents = {}, options = {}", subscriberId, aggregatesAndEvents, subscriberOptions);
    try {
      target.subscribe(subscriberId, aggregatesAndEvents, subscriberOptions, handler).get(timeoutOptions.getTimeout(), timeoutOptions.getTimeUnit());
      logger.info("Subscribed: subscriberId = {}, aggregatesAndEvents = {}, options = {}", subscriberId, aggregatesAndEvents, subscriberOptions);
    } catch (Throwable e) {
      logger.error("Subscription failed", e);

      Throwable unwrapped = CompletableFutureUtil.unwrap(e);
      if (unwrapped instanceof RuntimeException)
        throw (RuntimeException)unwrapped;
      else
        throw new RuntimeException(unwrapped);
    }
  }

  public void setTimeoutOptions(AsyncToSyncTimeoutOptions timeoutOptions) {
    this.timeoutOptions = timeoutOptions;
  }
}
