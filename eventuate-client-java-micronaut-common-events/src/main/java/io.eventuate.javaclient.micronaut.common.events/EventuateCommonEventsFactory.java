package io.eventuate.javaclient.micronaut.common.events;

import io.eventuate.EventuateAggregateStoreEvents;
import io.eventuate.javaclient.commonimpl.common.schema.EventuateEventSchemaManager;
import io.eventuate.javaclient.commonimpl.events.AggregateEvents;
import io.eventuate.javaclient.commonimpl.events.EventuateAggregateStoreEventsImpl;
import io.eventuate.javaclient.commonimpl.events.SerializedEventDeserializer;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.Nullable;

import javax.inject.Singleton;

@Factory
public class EventuateCommonEventsFactory {

  @Singleton
  public EventuateAggregateStoreEvents eventuateAggregateStoreEvents(AggregateEvents aggregateEvents,
                                                                     EventuateEventSchemaManager eventuateEventSchemaManager,
                                                                     @Nullable SerializedEventDeserializer serializedEventDeserializer) {
    EventuateAggregateStoreEventsImpl eventuateAggregateStoreEvents =
            new EventuateAggregateStoreEventsImpl(aggregateEvents, eventuateEventSchemaManager);

    if (serializedEventDeserializer != null)
      eventuateAggregateStoreEvents.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStoreEvents;
  }

  @Singleton
  public io.eventuate.sync.EventuateAggregateStoreEvents syncEventuateAggregateStoreEvents(io.eventuate.javaclient.commonimpl.events.sync.AggregateEvents aggregateEvents,
                                                                                           @Nullable SerializedEventDeserializer serializedEventDeserializer) {
    io.eventuate.javaclient.commonimpl.events.sync.EventuateAggregateStoreEventsImpl eventuateAggregateStoreEvents =
            new io.eventuate.javaclient.commonimpl.events.sync.EventuateAggregateStoreEventsImpl(aggregateEvents);

    if (serializedEventDeserializer != null)
      eventuateAggregateStoreEvents.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStoreEvents;
  }
}
