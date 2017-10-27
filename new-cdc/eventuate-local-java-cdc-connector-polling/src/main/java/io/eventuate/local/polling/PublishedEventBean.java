package io.eventuate.local.polling;

import java.util.Optional;

public class PublishedEventBean {
  private String eventId;
  private String eventType;
  private String eventData;
  private String entityType;
  private String entityId;
  private String triggeringEvent;
  private String metadata;

  public PublishedEventBean() {
  }

  public PublishedEventBean(String eventId,
          String eventType,
          String eventData,
          String entityType,
          String entityId,
          String triggeringEvent,
          String metadata) {

    this.eventId = eventId;
    this.eventType = eventType;
    this.eventData = eventData;
    this.entityType = entityType;
    this.entityId = entityId;
    this.triggeringEvent = triggeringEvent;
    this.metadata = metadata;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEventData() {
    return eventData;
  }

  public void setEventData(String eventData) {
    this.eventData = eventData;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getTriggeringEvent() {
    return triggeringEvent;
  }

  public void setTriggeringEvent(String triggeringEvent) {
    this.triggeringEvent = triggeringEvent;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public Optional<String> getMetadataOptional() {
    return Optional.ofNullable(metadata);
  }

  public void setMetadataOptional(Optional<String> metadata) {
    this.metadata = metadata.orElse(null);
  }
}
