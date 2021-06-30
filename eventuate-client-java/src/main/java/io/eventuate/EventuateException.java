package io.eventuate;

public class EventuateException extends RuntimeException {

  public EventuateException() {
  }

  public EventuateException(String m) {
    super(m);
  }

  public EventuateException(String m, Throwable e) {
    super(m, e);
  }

  public EventuateException(Throwable t) {
    super(t);
  }
}
