package io.eventuate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class EventContext {

  private String eventToken;

  public EventContext() {
  }

  public EventContext(String eventToken) {
    this.eventToken = eventToken;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public String getEventToken() {
    return eventToken;
  }

  public void setEventToken(String eventToken) {
    this.eventToken = eventToken;
  }
}
