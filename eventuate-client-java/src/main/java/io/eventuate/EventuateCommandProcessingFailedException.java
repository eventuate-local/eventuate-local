package io.eventuate;

public class EventuateCommandProcessingFailedException extends EventuateClientException {
  public EventuateCommandProcessingFailedException(Throwable t) {
    super(t);
  }
}
