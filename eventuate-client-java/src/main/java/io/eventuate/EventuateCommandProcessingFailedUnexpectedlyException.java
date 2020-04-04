package io.eventuate;

public class EventuateCommandProcessingFailedUnexpectedlyException extends EventuateClientException {
  public EventuateCommandProcessingFailedUnexpectedlyException(Throwable t) {
    super(t);
  }
}
