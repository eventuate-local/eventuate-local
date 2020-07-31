package io.eventuate.javaclient.micronaut.events;

import io.eventuate.EventuateAggregateStoreEvents;
import io.eventuate.SubscriptionsRegistry;
import io.eventuate.javaclient.domain.*;
import io.eventuate.javaclient.eventdispatcher.EventDispatcherInitializer;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;
import java.util.concurrent.Executors;

@Factory
public class EventuateJavaClientDomainEventsFactory {

  @Singleton
  public EventDispatcherInitializer eventDispatcherInitializer(EventHandlerProcessor[] processors, EventuateAggregateStoreEvents aggregateStore, SubscriptionsRegistry subscriptionsRegistry) {
    return new EventDispatcherInitializer(processors, aggregateStore, Executors.newCachedThreadPool(), subscriptionsRegistry);
  }

  @Singleton
  public SubscriptionsRegistry subscriptionsRegistry() {
    return new SubscriptionsRegistry();
  }

  @Singleton
  public EventHandlerProcessor eventHandlerProcessorDispatchedEventReturningVoid() {
    return new EventHandlerProcessorDispatchedEventReturningVoid();
  }

  @Singleton
  public EventHandlerProcessor eventHandlerProcessorDispatchedEventReturningCompletableFuture() {
    return new EventHandlerProcessorDispatchedEventReturningCompletableFuture();
  }
}
