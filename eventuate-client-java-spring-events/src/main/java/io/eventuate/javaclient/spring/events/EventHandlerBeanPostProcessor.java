package io.eventuate.javaclient.spring.events;

import io.eventuate.EventSubscriber;
import io.eventuate.javaclient.eventdispatcher.EventDispatcherInitializer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Registers Spring beans annotated with @EventSubscriber as event handlers
 *
 * @see io.eventuate.EventSubscriber
 */
public class EventHandlerBeanPostProcessor implements BeanPostProcessor {

  private EventDispatcherInitializer eventDispatcherInitializer;

  public EventHandlerBeanPostProcessor(EventDispatcherInitializer eventDispatcherInitializer) {
    this.eventDispatcherInitializer = eventDispatcherInitializer;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> actualClass = AopUtils.getTargetClass(bean);
    EventSubscriber a = AnnotationUtils.findAnnotation(actualClass, EventSubscriber.class);
    if (a != null)
      eventDispatcherInitializer.registerEventHandler(bean, beanName, actualClass);
    return bean;
  }
}
