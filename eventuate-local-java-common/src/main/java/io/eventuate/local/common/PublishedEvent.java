package io.eventuate.local.common;

/**
 * An event that is published to Kafka
 */
public class PublishedEvent {

  private String id;
  private String entityId;
  private String entityType;
  private String eventData;
  private String eventType;

  public PublishedEvent() {
  }

  public PublishedEvent(String id, String entityId, String entityType, String eventData, String eventType) {
    this.id = id;
    this.entityId = entityId;
    this.entityType = entityType;
    this.eventData = eventData;
    this.eventType = eventType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setEventData(String eventData) {
    this.eventData = eventData;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEntityId() {
    return entityId;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getEventData() {
    return eventData;
  }

  public String getEventType() {
    return eventType;
  }
}
