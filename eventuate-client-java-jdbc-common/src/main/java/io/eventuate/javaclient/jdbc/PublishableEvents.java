package io.eventuate.javaclient.jdbc;

import io.eventuate.javaclient.commonimpl.EventIdTypeAndData;

import java.util.List;


public class PublishableEvents {
  private final String aggregateType;
  private final String entityId;
  private final List<EventIdTypeAndData> eventsWithIds;

  public PublishableEvents(String aggregateType, String entityId, List<EventIdTypeAndData> eventsWithIds) {
    this.aggregateType = aggregateType;
    this.entityId = entityId;
    this.eventsWithIds = eventsWithIds;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public String getEntityId() {
    return entityId;
  }

  public List<EventIdTypeAndData> getEventsWithIds() {
    return eventsWithIds;
  }
}
