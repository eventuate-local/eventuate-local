package io.eventuate.javaclient.commonimpl.common.schema;

public class EventRename {
  private final String oldEventName;
  private final String newEventName;

  public EventRename(String oldEventName, String newEventName) {
    this.oldEventName = oldEventName;
    this.newEventName = newEventName;
  }

  public boolean isFor(String eventType) {
    return this.oldEventName.equals(eventType);
  }

  public String getNewEventName() {
    return newEventName;
  }
}
