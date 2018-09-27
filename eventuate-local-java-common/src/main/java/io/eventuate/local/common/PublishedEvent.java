package io.eventuate.local.common;

import java.util.Optional;

/**
 * An event that is published to Kafka
 */
public class PublishedEvent implements BinLogEvent {

  private String id;
  private String entityId;
  private String entityType;
  private String eventData;
  private String eventType;
  private BinlogFileOffset binlogFileOffset;
  private Optional<String> metadata;

  public PublishedEvent() {
  }

  public PublishedEvent(String id, String entityId, String entityType, String eventData, String eventType, BinlogFileOffset binlogFileOffset, Optional<String> metadata) {
    this.id = id;
    this.entityId = entityId;
    this.entityType = entityType;
    this.eventData = eventData;
    this.eventType = eventType;
    this.binlogFileOffset = binlogFileOffset;
    this.metadata = metadata;
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

  public Optional<String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Optional<String> metadata) {
    this.metadata = metadata;
  }

  @Override
  public Optional<BinlogFileOffset> getBinlogFileOffset() {
    return Optional.ofNullable(binlogFileOffset);
  }

  public void setBinlogFileOffset(BinlogFileOffset binlogFileOffset) {
    this.binlogFileOffset = binlogFileOffset;
  }
}
