package io.eventuate;

public class EventuateClientException extends EventuateException {

  public EventuateClientException() {
  }

  public EventuateClientException(Throwable t) {
    super(t);
  }

  public EventuateClientException(String m) {
    super(m);
  }
}
