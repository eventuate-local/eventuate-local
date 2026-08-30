package io.eventuate.javaclient.spring.common.events;

import io.eventuate.EventuateAggregateStoreEvents;
import io.eventuate.javaclient.commonimpl.events.AggregateEvents;
import io.eventuate.javaclient.commonimpl.events.EventuateAggregateStoreEventsImpl;
import io.eventuate.javaclient.commonimpl.events.SerializedEventDeserializer;
import io.eventuate.javaclient.commonimpl.common.schema.EventuateEventSchemaManager;
import io.eventuate.javaclient.spring.common.common.EventuateCommonCommonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateCommonCommonConfiguration.class)
public class EventuateCommonEventsConfiguration {

  @Autowired(required=false)
  private SerializedEventDeserializer serializedEventDeserializer;

  @Bean
  public EventuateAggregateStoreEvents eventuateAggregateStoreEvents(AggregateEvents aggregateEvents,
                                                                     EventuateEventSchemaManager eventuateEventSchemaManager) {
    EventuateAggregateStoreEventsImpl eventuateAggregateStoreEvents =
            new EventuateAggregateStoreEventsImpl(aggregateEvents, eventuateEventSchemaManager);

    if (serializedEventDeserializer != null)
      eventuateAggregateStoreEvents.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStoreEvents;
  }

  @Bean
  public io.eventuate.sync.EventuateAggregateStoreEvents syncEventuateAggregateStoreEvents(io.eventuate.javaclient.commonimpl.events.sync.AggregateEvents aggregateEvents) {
    io.eventuate.javaclient.commonimpl.events.sync.EventuateAggregateStoreEventsImpl eventuateAggregateStoreEvents =
            new io.eventuate.javaclient.commonimpl.events.sync.EventuateAggregateStoreEventsImpl(aggregateEvents);

    if (serializedEventDeserializer != null)
      eventuateAggregateStoreEvents.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStoreEvents;
  }
}
