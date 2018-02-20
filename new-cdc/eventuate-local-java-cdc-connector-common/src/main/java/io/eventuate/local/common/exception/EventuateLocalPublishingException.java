package io.eventuate.local.common.exception;

public class EventuateLocalPublishingException extends RuntimeException {

  public EventuateLocalPublishingException(String message, Exception cause) {
    super(message, cause);
  }
}
