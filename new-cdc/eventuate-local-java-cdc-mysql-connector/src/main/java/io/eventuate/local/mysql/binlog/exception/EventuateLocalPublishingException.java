package io.eventuate.local.mysql.binlog.exception;

public class EventuateLocalPublishingException extends Exception {

  public EventuateLocalPublishingException(String message, Exception cause) {
    super(message, cause);
  }

}
