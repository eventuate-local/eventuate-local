package io.eventuate.testutil;

import io.eventuate.Event;
import io.eventuate.common.id.Int128;

import java.util.Map;
import java.util.Optional;

public class ReceivedEvent {

  private Class<Event> eventType;
  private String entityId;
  private final Int128 eventId;
  private final Event event;
  private Optional<Map<String, String>> eventMetadata;


  public ReceivedEvent(String entityId, Class<Event> eventType, Int128 eventId, Event event, Optional<Map<String, String>> eventMetadata) {
    this.entityId = entityId;
    this.eventType = eventType;
    this.eventId = eventId;
    this.event = event;
    this.eventMetadata = eventMetadata;
  }

  public String getEntityId() {
    return entityId;
  }

  public Int128 getEventId() {
    return eventId;
  }

  public Event getEvent() {
    return event;
  }

  public Class<Event> getEventType() {

    return eventType;
  }

  public Optional<Map<String, String>> getEventMetadata() {
    return eventMetadata;
  }
}
