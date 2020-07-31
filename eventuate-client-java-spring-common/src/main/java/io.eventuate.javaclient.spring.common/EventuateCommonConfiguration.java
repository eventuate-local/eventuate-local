package io.eventuate.javaclient.spring.common;

import io.eventuate.EventuateAggregateStore;
import io.eventuate.EventuateAggregateStoreCrud;
import io.eventuate.EventuateAggregateStoreEvents;
import io.eventuate.javaclient.commonimpl.EventuateAggregateStoreImpl;
import io.eventuate.javaclient.spring.common.crud.EventuateCommonCrudConfiguration;
import io.eventuate.javaclient.spring.common.events.EventuateCommonEventsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import({EventuateCommonCrudConfiguration.class, EventuateCommonEventsConfiguration.class})
public class EventuateCommonConfiguration {
  @Bean
  @Primary
  public EventuateAggregateStore aggregateEventStore(EventuateAggregateStoreCrud eventuateAggregateStoreCrud,
                                                     EventuateAggregateStoreEvents eventuateAggregateStoreEvents) {

    return new EventuateAggregateStoreImpl(eventuateAggregateStoreCrud, eventuateAggregateStoreEvents);
  }

  @Bean
  @Primary
  public io.eventuate.sync.EventuateAggregateStore syncAggregateEventStore(io.eventuate.sync.EventuateAggregateStoreCrud eventuateAggregateStoreCrud,
                                                                           io.eventuate.sync.EventuateAggregateStoreEvents eventuateAggregateStoreEvents) {

    return new io.eventuate.javaclient.commonimpl.sync.EventuateAggregateStoreImpl(eventuateAggregateStoreCrud, eventuateAggregateStoreEvents);
  }
}
