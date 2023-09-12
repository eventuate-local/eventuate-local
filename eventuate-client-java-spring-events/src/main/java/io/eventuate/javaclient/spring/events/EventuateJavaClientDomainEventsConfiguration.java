package io.eventuate.javaclient.spring.events;

import io.eventuate.EventuateAggregateStoreEvents;
import io.eventuate.SubscriptionsRegistry;
import io.eventuate.javaclient.domain.*;
import io.eventuate.javaclient.eventdispatcher.EventDispatcherInitializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class EventuateJavaClientDomainEventsConfiguration {
  @Bean
  @ConditionalOnMissingBean(EventHandlerBeanPostProcessor.class)
  public EventHandlerBeanPostProcessor eventHandlerBeanPostProcessor(EventDispatcherInitializer eventDispatcherInitializer) {
    return new EventHandlerBeanPostProcessor(eventDispatcherInitializer);
  }

  @Bean
  @ConditionalOnMissingBean(EventDispatcherInitializer.class)
  public EventDispatcherInitializer eventDispatcherInitializer(EventHandlerProcessor[] processors, EventuateAggregateStoreEvents aggregateStore, SubscriptionsRegistry subscriptionsRegistry) {
    return new EventDispatcherInitializer(processors, aggregateStore, Executors.newCachedThreadPool(), subscriptionsRegistry);
  }

  @Bean
  public SubscriptionsRegistry subscriptionsRegistry() {
    return new SubscriptionsRegistry();
  }

  @Bean
  public EventHandlerProcessor eventHandlerProcessorDispatchedEventReturningVoid() {
    return new EventHandlerProcessorDispatchedEventReturningVoid();
  }

  @Bean
  public EventHandlerProcessor eventHandlerProcessorDispatchedEventReturningCompletableFuture() {
    return new EventHandlerProcessorDispatchedEventReturningCompletableFuture();
  }
}
