package io.eventuate.javaclient.eventhandling.exceptionhandling;

import java.util.function.Consumer;

/**
 * Asynchronous handler for an exception thrown by an event handler
 */
public interface EventDeliveryExceptionHandler {

  /**
   * Returns true if this handles the throwable
   * @param t the throwable
   * @return true if handled, false otherwise
   */
  boolean handles(Throwable t);

  /**
   * create the state used for handling an exception
   * @param t the throwable
   * @return the state used by the handler
   */
  EventDeliveryExceptionHandlerState makeState(Throwable t);

  /**
   * handle an exception by either (asynchronously) retrying or failing
   * @param state the state
   * @param t the throwable
   * @param retry invoked asynchronously to retry
   * @param fail invoked asynchronously to fail
   * @param ignore invoked to ignore the exception
   */
  void handle(EventDeliveryExceptionHandlerState state, Throwable t, Runnable retry, Consumer<Throwable> fail, Runnable ignore);


}
