package io.eventuate.javaclient.eventhandling.exceptionhandling;

public class RetryEventDeliveryExceptionHandlerState implements EventDeliveryExceptionHandlerState {
  public int retries;

  public RetryEventDeliveryExceptionHandlerState(Throwable t) {
    this.retries = 0;
  }

}
