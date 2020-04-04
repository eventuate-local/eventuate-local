package io.eventuate.testutil;

public class EventuallyException extends RuntimeException {
  public EventuallyException(String message, Throwable cause) {
    super(message, cause);
  }
}
