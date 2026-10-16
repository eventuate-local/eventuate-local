package io.eventuate;

public class MissingApplyMethodException extends EventuateApplyEventFailedUnexpectedlyException {
  private final Event event;

  public MissingApplyMethodException(Throwable  e, Event event) {
    super(e);
    this.event = event;
  }

  public Event getEvent() {
    return event;
  }
}
