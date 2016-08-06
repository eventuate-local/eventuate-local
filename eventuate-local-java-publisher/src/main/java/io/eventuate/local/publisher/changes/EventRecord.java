package io.eventuate.local.publisher.changes;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A change to the Events table captured by Debezium
 */
public class EventRecord {

  private EventPayload payload;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public EventPayload getPayload() {
    return payload;
  }

  public void setPayload(EventPayload payload) {
    this.payload = payload;
  }
}
