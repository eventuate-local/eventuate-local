package io.eventuate.javaclient.domain;

import io.eventuate.CompletableFutureUtil;
import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventDeliveryExceptionHandlerManager;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventDeliveryExceptionHandlerWithState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.eventuate.javaclient.commonimpl.common.EventuateActivity.activityLogger;

public class EventDispatcher {

  private static Logger logger = LoggerFactory.getLogger(EventDispatcher.class);

  private final String subscriberId;
  private final Map<Class<?>, EventHandler> eventTypesAndHandlers;
  private EventDeliveryExceptionHandlerManager eventDeliveryExceptionHandlerManager;

  public EventDispatcher(String subscriberId, Map<Class<?>, EventHandler> eventTypesAndHandlers, EventDeliveryExceptionHandlerManager eventDeliveryExceptionHandlerManager) {
    this.subscriberId = subscriberId;
    this.eventTypesAndHandlers = eventTypesAndHandlers;
    this.eventDeliveryExceptionHandlerManager = eventDeliveryExceptionHandlerManager;
  }

  public CompletableFuture<?> dispatch(DispatchedEvent<Event> de) {
    EventHandler eventHandler = eventTypesAndHandlers.get(de.getEventType());
    if (eventHandler != null) {
      CompletableFuture<Object> result = new CompletableFuture<>();
      dispatchEvent(de, eventHandler, result, Optional.empty());
      return result;
    } else {
      RuntimeException ex = new RuntimeException("No handler for event - subscriberId: " + subscriberId + ", " + de.getEventType());
      logger.error("dispatching failure", ex);
      CompletableFuture completableFuture = new CompletableFuture();
      completableFuture.completeExceptionally(ex);
      return completableFuture;
    }
  }

  private void dispatchEvent(DispatchedEvent<Event> de, EventHandler eventHandler, CompletableFuture<Object> result, Optional<EventDeliveryExceptionHandlerWithState> ehr) {
    if (activityLogger.isDebugEnabled()) {
      activityLogger.debug("Invoking event handler {} {} {}", subscriberId, de, eventHandler);
    }

    CompletableFuture<?> r1 = eventHandler.dispatch(de);

    if (activityLogger.isDebugEnabled()) {
      CompletableFutureUtil.tap(r1, (o, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Invoked event handler {} {} {}", subscriberId, de, eventHandler);
        else
          activityLogger.debug(String.format("Event handler failed %s %s %s", subscriberId, de, eventHandler), throwable);
      });
    }

    r1.handle((o, t) -> {
              if (t == null) {
                result.complete(o);
              } else {
                Throwable t1 = CompletableFutureUtil.unwrap(unwrap(CompletableFutureUtil.unwrap(t)));
                EventDeliveryExceptionHandlerWithState eventDeliveryExceptionHandlerWithState = ehr.orElseGet(() -> eventDeliveryExceptionHandlerManager.getEventHandler(t1));
                eventDeliveryExceptionHandlerWithState.handle(t1,
                        () -> dispatchEvent(de, eventHandler, result, Optional.of(eventDeliveryExceptionHandlerWithState)),
                        result::completeExceptionally,
                        () -> result.complete(null));
              }
              return null;
            }
    );
  }

  private Throwable unwrap(Throwable t1) {
    return t1 instanceof InvocationTargetException ? t1.getCause() : t1;
  }

}
