package io.eventuate.javaclient.micronaut;

import io.eventuate.EventuateAggregateStoreCrud;
import io.eventuate.EventuateAggregateStoreEvents;
import io.eventuate.SubscriptionsRegistry;
import io.eventuate.javaclient.domain.EventHandlerProcessor;
import io.eventuate.javaclient.domain.EventHandlerProcessorDispatchedEventReturningCompletableFuture;
import io.eventuate.javaclient.domain.EventHandlerProcessorDispatchedEventReturningVoid;
import io.eventuate.javaclient.domain.EventHandlerProcessorEventHandlerContextReturningCompletableFuture;
import io.eventuate.javaclient.domain.EventHandlerProcessorEventHandlerContextReturningVoid;
import io.eventuate.javaclient.eventdispatcher.EventDispatcherInitializer;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;
import java.util.concurrent.Executors;

@Factory
public class EventuateJavaClientDomainFactory {

  @Singleton
  public EventDispatcherInitializer eventDispatcherInitializer(EventHandlerProcessor[] processors, EventuateAggregateStoreEvents aggregateStore, SubscriptionsRegistry subscriptionsRegistry) {
    return new EventDispatcherInitializer(processors, aggregateStore, Executors.newCachedThreadPool(), subscriptionsRegistry);
  }

  @Singleton
  public SubscriptionsRegistry subscriptionsRegistry() {
    return new SubscriptionsRegistry();
  }

  @Singleton
  public EventHandlerProcessor eventHandlerProcessorEventHandlerContextReturningVoid(EventuateAggregateStoreCrud aggregateStore) {
    return new EventHandlerProcessorEventHandlerContextReturningVoid(aggregateStore);
  }

  @Singleton
  public EventHandlerProcessor eventHandlerProcessorDispatchedEventReturningVoid() {
    return new EventHandlerProcessorDispatchedEventReturningVoid();
  }

  @Singleton
  public EventHandlerProcessor eventHandlerProcessorDispatchedEventReturningCompletableFuture() {
    return new EventHandlerProcessorDispatchedEventReturningCompletableFuture();
  }

  @Singleton
  public EventHandlerProcessor eventHandlerProcessorEventHandlerContextReturningCompletableFuture(EventuateAggregateStoreCrud aggregateStore) {
    return new EventHandlerProcessorEventHandlerContextReturningCompletableFuture(aggregateStore);
  }

}
