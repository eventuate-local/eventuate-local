package io.eventuate.javaclient.eventhandling.exceptionhandling;

import java.util.function.Consumer;

/**
 * An exception handler that fails immediately
 */
public class FailEventDeliveryExceptionHandler implements EventDeliveryExceptionHandler {

  @Override
  public boolean handles(Throwable t) {
    return true;
  }

  @Override
  public EventDeliveryExceptionHandlerState makeState(Throwable t) {
    return null;
  }

  @Override
  public void handle(EventDeliveryExceptionHandlerState state, Throwable t, Runnable retry, Consumer<Throwable> fail, Runnable ignore) {
    fail.accept(t);
  }
}
