package io.eventuate.javaclient.commonimpl;

import java.util.Optional;

public class EventTypeAndData {

  private String eventType;
  private String eventData;
  private Optional<String> metadata;

  public EventTypeAndData(String eventType, String eventData, Optional<String> metadata) {
    this.eventType = eventType;
    this.eventData = eventData;
    this.metadata = metadata;
  }

  public String getEventType() {
    return eventType;
  }

  public String getEventData() {
    return eventData;
  }

  public Optional<String> getMetadata() {
    return metadata;
  }
}
