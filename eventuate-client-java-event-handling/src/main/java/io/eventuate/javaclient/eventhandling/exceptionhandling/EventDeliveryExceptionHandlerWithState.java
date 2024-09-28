package io.eventuate.javaclient.eventhandling.exceptionhandling;

import java.util.function.Consumer;

public class EventDeliveryExceptionHandlerWithState {

  private EventDeliveryExceptionHandler eventDeliveryExceptionHandler;
  private EventDeliveryExceptionHandlerState state;

  public EventDeliveryExceptionHandlerWithState(EventDeliveryExceptionHandler eventDeliveryExceptionHandler, EventDeliveryExceptionHandlerState state) {
    this.eventDeliveryExceptionHandler = eventDeliveryExceptionHandler;
    this.state = state;
  }

  public void handle(Throwable t, Runnable retry, Consumer<Throwable> fail, Runnable ignore) {
    eventDeliveryExceptionHandler.handle(state, t, retry, fail, ignore);
  }

}
