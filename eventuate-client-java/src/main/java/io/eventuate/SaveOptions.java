package io.eventuate;

import java.util.Map;
import java.util.Optional;

public class SaveOptions {

  private Optional<String> entityId = Optional.empty();
  private Optional<EventContext> triggeringEvent = Optional.empty();
  private Optional<Map<String, String>> eventMetadata = Optional.empty();

  public Optional<String> getEntityId() {
    return entityId;
  }

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }

  public Optional<Map<String, String>> getEventMetadata() {
    return eventMetadata;
  }

  public SaveOptions withId(String entityId) {
    this.entityId = Optional.of(entityId);
    return this;
  }

  public SaveOptions withId(Optional<String> entityId) {
    this.entityId = entityId;
    return this;
  }

  public SaveOptions withEventContext(EventContext eventContext) {
    this.triggeringEvent = Optional.of(eventContext);
    return this;
  }

  public SaveOptions withEventMetadata(Map<String, String> eventMetadata) {
    this.eventMetadata = Optional.of(eventMetadata);
    return this;
  }
}
