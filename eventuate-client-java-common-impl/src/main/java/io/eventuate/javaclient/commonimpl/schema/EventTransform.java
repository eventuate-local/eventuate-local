package io.eventuate.javaclient.commonimpl.schema;

public class EventTransform {
  private final String eventName;
  private final EventUpcaster upcaster;

  public EventTransform(String eventName, EventUpcaster upcaster) {
    this.eventName = eventName;
    this.upcaster = upcaster;
  }

  public boolean isFor(String eventType) {
    return this.eventName.equals(eventType);
  }

  public EventUpcaster getUpcaster() {
    return upcaster;
  }
}
