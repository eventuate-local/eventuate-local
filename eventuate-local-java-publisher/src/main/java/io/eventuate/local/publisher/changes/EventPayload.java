package io.eventuate.local.publisher.changes;

import org.apache.commons.lang.builder.ToStringBuilder;

public class EventPayload {

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  private EventInfo after;

  public EventInfo getAfter() {

    return after;
  }

  public void setAfter(EventInfo after) {
    this.after = after;
  }
}
