package io.eventuate.javaclient.spring;

import io.eventuate.EventuateAggregateStoreCrud;
import io.eventuate.EventuateAggregateStoreEvents;
import io.eventuate.SubscriptionsRegistry;
import io.eventuate.javaclient.domain.EventHandlerProcessor;
import io.eventuate.javaclient.domain.EventHandlerProcessorDispatchedEventReturningCompletableFuture;
import io.eventuate.javaclient.domain.EventHandlerProcessorDispatchedEventReturningVoid;
import io.eventuate.javaclient.domain.EventHandlerProcessorEventHandlerContextReturningCompletableFuture;
import io.eventuate.javaclient.domain.EventHandlerProcessorEventHandlerContextReturningVoid;
import io.eventuate.javaclient.eventdispatcher.EventDispatcherInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * Defines the Spring beans that support event processing
 */
@Configuration
public class EventuateJavaClientDomainConfiguration {

  @Bean
  public EventHandlerBeanPostProcessor eventHandlerBeanPostProcessor(EventDispatcherInitializer eventDispatcherInitializer) {
    return new EventHandlerBeanPostProcessor(eventDispatcherInitializer);
  }

  @Bean
  public EventDispatcherInitializer eventDispatcherInitializer(EventHandlerProcessor[] processors, EventuateAggregateStoreEvents aggregateStore, SubscriptionsRegistry subscriptionsRegistry) {
    return new EventDispatcherInitializer(processors, aggregateStore, Executors.newCachedThreadPool(), subscriptionsRegistry);
  }

  @Bean
  public SubscriptionsRegistry subscriptionsRegistry() {
    return new SubscriptionsRegistry();
  }

  @Bean
  public EventHandlerProcessor eventHandlerProcessorEventHandlerContextReturningVoid(EventuateAggregateStoreCrud aggregateStore) {
    return new EventHandlerProcessorEventHandlerContextReturningVoid(aggregateStore);
  }

  @Bean
  public EventHandlerProcessor eventHandlerProcessorDispatchedEventReturningVoid() {
    return new EventHandlerProcessorDispatchedEventReturningVoid();
  }

  @Bean
  public EventHandlerProcessor eventHandlerProcessorDispatchedEventReturningCompletableFuture() {
    return new EventHandlerProcessorDispatchedEventReturningCompletableFuture();
  }
  @Bean
  public EventHandlerProcessor eventHandlerProcessorEventHandlerContextReturningCompletableFuture(EventuateAggregateStoreCrud aggregateStore) {
    return new EventHandlerProcessorEventHandlerContextReturningCompletableFuture(aggregateStore);
  }

}
