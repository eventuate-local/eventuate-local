package io.eventuate.javaclient.commonimpl.events.sync;

import io.eventuate.*;
import io.eventuate.javaclient.commonimpl.events.DefaultSerializedEventDeserializer;
import io.eventuate.javaclient.commonimpl.events.SerializedEventDeserializer;
import io.eventuate.sync.EventuateAggregateStoreEvents;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static io.eventuate.javaclient.commonimpl.common.EventuateActivity.activityLogger;

public class EventuateAggregateStoreEventsImpl implements EventuateAggregateStoreEvents {

  private AggregateEvents aggregateEvents;
  private SerializedEventDeserializer serializedEventDeserializer = new DefaultSerializedEventDeserializer();

  public EventuateAggregateStoreEventsImpl(AggregateEvents aggregateEvents) {
    this.aggregateEvents = aggregateEvents;
  }

  public void setSerializedEventDeserializer(SerializedEventDeserializer serializedEventDeserializer) {
    this.serializedEventDeserializer = serializedEventDeserializer;
  }

  @Override
  public void subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<DispatchedEvent<Event>, CompletableFuture<?>> handler) {
    try {
      if (activityLogger.isDebugEnabled())
        activityLogger.debug("Subscribing {} {}", subscriberId, aggregatesAndEvents);
      aggregateEvents.subscribe(subscriberId, aggregatesAndEvents, subscriberOptions,
              se -> serializedEventDeserializer.toDispatchedEvent(se).map(handler::apply).orElse(CompletableFuture.completedFuture(null)));
      if (activityLogger.isDebugEnabled())
        activityLogger.debug("Subscribed {} {}", subscriberId, aggregatesAndEvents);
    } catch (Exception e) {
      if (activityLogger.isDebugEnabled())
        activityLogger.error(String.format("Subscribe failed: %s %s", subscriberId, aggregatesAndEvents), e);
      throw e;
    }
  }
}
