package io.eventuate.javaclient.micronaut.crud;

import io.eventuate.EventuateAggregateStoreCrud;
import io.eventuate.javaclient.domain.EventHandlerProcessor;
import io.eventuate.javaclient.domain.EventHandlerProcessorEventHandlerContextReturningCompletableFuture;
import io.eventuate.javaclient.domain.EventHandlerProcessorEventHandlerContextReturningVoid;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class EventuateJavaClientDomainCrudFactory {

  @Singleton
  public EventHandlerProcessor eventHandlerProcessorEventHandlerContextReturningVoid(EventuateAggregateStoreCrud aggregateStore) {
    return new EventHandlerProcessorEventHandlerContextReturningVoid(aggregateStore);
  }

  @Singleton
  public EventHandlerProcessor eventHandlerProcessorEventHandlerContextReturningCompletableFuture(EventuateAggregateStoreCrud aggregateStore) {
    return new EventHandlerProcessorEventHandlerContextReturningCompletableFuture(aggregateStore);
  }

}
