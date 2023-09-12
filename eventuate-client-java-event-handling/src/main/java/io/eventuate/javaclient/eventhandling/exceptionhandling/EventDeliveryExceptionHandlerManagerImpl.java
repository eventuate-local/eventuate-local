package io.eventuate.javaclient.eventhandling.exceptionhandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class EventDeliveryExceptionHandlerManagerImpl implements EventDeliveryExceptionHandlerManager {

  private final Logger logger = LoggerFactory.getLogger(EventDeliveryExceptionHandlerManagerImpl.class);
  private List<EventDeliveryExceptionHandler> exceptionHandlers;

  public EventDeliveryExceptionHandlerManagerImpl(List<EventDeliveryExceptionHandler> exceptionHandlers) {
    this.exceptionHandlers = exceptionHandlers;
  }

  @Override
  public EventDeliveryExceptionHandlerWithState getEventHandler(Throwable t) {
    EventDeliveryExceptionHandler eh = exceptionHandlers.stream()
            .filter(eh1 -> eh1.handles(t))
            .findFirst()
            .orElseGet(FailEventDeliveryExceptionHandler::new);

    return new EventDeliveryExceptionHandlerWithState(eh, eh.makeState(t));

  }

}
