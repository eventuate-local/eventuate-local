package io.eventuate.javaclient.micronaut;

import io.eventuate.Subscriber;
import io.eventuate.javaclient.eventdispatcher.EventDispatcherInitializer;
import io.micronaut.context.annotation.Context;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Context
public class EventHandlerRegistrar {

  private EventDispatcherInitializer eventDispatcherInitializer;
  private Subscriber[] subscribers;

  public EventHandlerRegistrar(EventDispatcherInitializer eventDispatcherInitializer, Subscriber[] subscribers) {
    this.eventDispatcherInitializer = eventDispatcherInitializer;
    this.subscribers = subscribers;
  }

  @PostConstruct
  public void registerEventHandlers() throws BeansException {
    for (Subscriber subscriber : subscribers) {
      Class<?> actualClass = AopUtils.getTargetClass(subscriber);
      String name = subscriber.getClass().getSimpleName();
      name = String.valueOf(name.charAt(0)).toLowerCase() + name.substring(1);
      eventDispatcherInitializer.registerEventHandler(subscriber, name, actualClass);
    }
  }
}
