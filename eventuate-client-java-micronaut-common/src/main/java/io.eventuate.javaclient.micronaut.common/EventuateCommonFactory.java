package io.eventuate.javaclient.micronaut.common;

import io.eventuate.*;
import io.eventuate.javaclient.commonimpl.EventuateAggregateStoreImpl;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class EventuateCommonFactory {

  @Singleton
  public EventuateAggregateStore aggregateEventStore(EventuateAggregateStoreCrud eventuateAggregateStoreCrud,
                                                     EventuateAggregateStoreEvents eventuateAggregateStoreEvents) {

    return new EventuateAggregateStoreImpl(eventuateAggregateStoreCrud, eventuateAggregateStoreEvents);
  }

  @Singleton
  public io.eventuate.sync.EventuateAggregateStore syncAggregateEventStore(io.eventuate.sync.EventuateAggregateStoreCrud eventuateAggregateStoreCrud,
                                                                           io.eventuate.sync.EventuateAggregateStoreEvents eventuateAggregateStoreEvents) {

    return new io.eventuate.javaclient.commonimpl.sync.EventuateAggregateStoreImpl(eventuateAggregateStoreCrud, eventuateAggregateStoreEvents);
  }
}
