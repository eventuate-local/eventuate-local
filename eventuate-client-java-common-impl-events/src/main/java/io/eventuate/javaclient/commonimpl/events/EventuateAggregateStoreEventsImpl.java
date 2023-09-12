package io.eventuate.javaclient.commonimpl.events;

import io.eventuate.*;
import io.eventuate.javaclient.commonimpl.common.schema.EventuateEventSchemaManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static io.eventuate.javaclient.commonimpl.common.EventuateActivity.activityLogger;

public class EventuateAggregateStoreEventsImpl implements EventuateAggregateStoreEvents {


  private AggregateEvents aggregateEvents;
  private SerializedEventDeserializer serializedEventDeserializer = new DefaultSerializedEventDeserializer();
  private EventuateEventSchemaManager eventuateEventSchemaManager;

  public EventuateAggregateStoreEventsImpl(AggregateEvents aggregateEvents,
                                           EventuateEventSchemaManager eventuateEventSchemaManager) {
    this.aggregateEvents = aggregateEvents;
    this.eventuateEventSchemaManager = eventuateEventSchemaManager;
  }

  public void setSerializedEventDeserializer(SerializedEventDeserializer serializedEventDeserializer) {
    this.serializedEventDeserializer = serializedEventDeserializer;
  }

  @Override
  public CompletableFuture<?> subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<DispatchedEvent<Event>, CompletableFuture<?>> handler) {
    if (activityLogger.isDebugEnabled())
      activityLogger.debug("Subscribing {} {}", subscriberId, aggregatesAndEvents);
    CompletableFuture<?> outcome = aggregateEvents.subscribe(subscriberId, aggregatesAndEvents, subscriberOptions,
            se -> serializedEventDeserializer.toDispatchedEvent(eventuateEventSchemaManager.upcastEvent(se)).map(handler::apply).orElse(CompletableFuture.completedFuture(null)));
    if (activityLogger.isDebugEnabled())
      return CompletableFutureUtil.tap(outcome, (result, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Subscribed {} {}", subscriberId, aggregatesAndEvents);
        else
          activityLogger.error(String.format("Subscribe failed: %s %s", subscriberId, aggregatesAndEvents), throwable);
      });
    else
      return outcome;
  }
}
