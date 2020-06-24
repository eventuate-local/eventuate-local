package io.eventuate.javaclient.eventdispatcher;

import io.eventuate.*;
import io.eventuate.javaclient.domain.EventDispatcher;
import io.eventuate.javaclient.domain.EventHandler;
import io.eventuate.javaclient.domain.EventHandlerProcessor;
import io.eventuate.javaclient.domain.SwimlaneBasedDispatcher;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventDeliveryExceptionHandlerManagerImpl;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventDeliveryExceptionHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class EventDispatcherInitializer {
  protected Logger logger = LoggerFactory.getLogger(getClass());

  private EventHandlerProcessor[] processors;
  private EventuateAggregateStoreEvents aggregateStore;
  private Executor executorService;
  private SubscriptionsRegistry subscriptionsRegistry;

  private Set<String> subscriberIds = new HashSet<>();

  public EventDispatcherInitializer(EventHandlerProcessor[] processors, EventuateAggregateStoreEvents aggregateStore, Executor executorService, SubscriptionsRegistry subscriptionsRegistry) {
    this.processors = processors;
    this.aggregateStore = aggregateStore;
    this.executorService = executorService;
    this.subscriptionsRegistry = subscriptionsRegistry;
  }


  public void registerEventHandler(Object eventHandlerBean, String beanName, Class<?> beanClass) {
    logger.info("registering event handler: bean: {}, name: {}, class", eventHandlerBean, beanName, beanClass);

    List<AccessibleObject> fieldsAndMethods = Stream.<AccessibleObject>concat(Arrays.stream(ReflectionUtils.getUniqueDeclaredMethods(beanClass)),
            Arrays.stream(beanClass.getDeclaredFields()))
            .collect(toList());

    List<AccessibleObject> annotatedCandidateEventHandlers = fieldsAndMethods.stream()
            .filter(fieldOrMethod -> AnnotationUtils.findAnnotation(fieldOrMethod, EventHandlerMethod.class) != null)
            .collect(toList());

    List<EventHandler> handlers = annotatedCandidateEventHandlers.stream()
            .map(fieldOrMethod -> Arrays.stream(processors).filter(processor -> processor.supports(fieldOrMethod)).findFirst().orElseThrow(() -> new RuntimeException("Don't know what to do with fieldOrMethod " + fieldOrMethod))
                    .process(eventHandlerBean, fieldOrMethod))
            .collect(toList());

    if (handlers.isEmpty())
      throw new RuntimeException("No handlers defined for this class" + beanClass);

    Map<String, Set<String>> aggregatesAndEvents = makeAggregatesAndEvents(handlers.stream()
            .filter(handler -> !handler.getEventType().equals(EndOfCurrentEventsReachedEvent.class)).collect(toList()));

    Map<Class<?>, EventHandler> eventTypesAndHandlers = makeEventTypesAndHandlers(handlers);

    List<EventDeliveryExceptionHandler> exceptionHandlers = Arrays.stream(beanClass
            .getDeclaredFields())
            .filter(this::isExceptionHandlerField)
            .map(f -> {
              try {
                f.setAccessible(true);
                return (EventDeliveryExceptionHandler) f.get(eventHandlerBean);
              } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
              }
            })
            .collect(toList());

    EventSubscriber a = AnnotationUtils.findAnnotation(beanClass, EventSubscriber.class);
    if (a == null)
      throw new RuntimeException("Needs @EventSubscriber annotation: " + eventHandlerBean);

    String subscriberId = StringUtils.isBlank(a.id()) ? beanName : a.id();

    EventDispatcher eventDispatcher = new EventDispatcher(subscriberId, eventTypesAndHandlers, new EventDeliveryExceptionHandlerManagerImpl(exceptionHandlers));

    SwimlaneBasedDispatcher swimlaneBasedDispatcher = new SwimlaneBasedDispatcher(subscriberId, executorService);


    if (subscriberIds.contains(subscriberId))
      throw new RuntimeException("Duplicate subscriptionId " + subscriberId);
    subscriberIds.add(subscriberId);

    SubscriberOptions subscriberOptions = new SubscriberOptions(a.durability(), a.readFrom(), a.progressNotifications());

    // TODO - it would be nice to do this in parallel
    try {
      aggregateStore.subscribe(subscriberId, aggregatesAndEvents,
              subscriberOptions, de -> swimlaneBasedDispatcher.dispatch(de, eventDispatcher::dispatch)).get(20, TimeUnit.SECONDS);
      subscriptionsRegistry.add(new RegisteredSubscription(subscriberId, aggregatesAndEvents, beanClass));
    } catch (InterruptedException | TimeoutException | ExecutionException e) {
      logger.error("registering event handler failed", e);
      throw new EventuateSubscriptionFailedException(subscriberId, e);
    }

    logger.info("registered event handler: bean: {}, name: {}, class", eventHandlerBean, beanName, beanClass);
  }

  private boolean isExceptionHandlerField(Field f) {
    return EventDeliveryExceptionHandler.class.isAssignableFrom(f.getType());
  }

  private Map<Class<?>, EventHandler> makeEventTypesAndHandlers(List<EventHandler> handlers) {
    // TODO - if keys are not unique you get an IllegalStateException
    // Need to provide a helpful error message
    return handlers.stream().collect(Collectors.toMap(EventHandler::getEventType, eh -> eh));
  }

  private Map<String, Set<String>> makeAggregatesAndEvents(List<EventHandler> handlers) {
    return handlers.stream().collect(Collectors.toMap(
            eh -> EventEntityUtil.toEntityTypeName(eh.getEventType()),
            eh -> Collections.singleton(eh.getEventType().getName()),
            (e1, e2) -> {
              HashSet<String> r = new HashSet<String>(e1);
              r.addAll(e2);
              return r;
            }
    ));
  }

}
