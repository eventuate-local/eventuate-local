package io.eventuate.javaclient.eventhandling.exceptionhandling;

public interface EventDeliveryExceptionHandlerManager {

  EventDeliveryExceptionHandlerWithState getEventHandler(Throwable t);


}
