package io.eventuate.javaclient.commonimpl;

import io.eventuate.common.id.Int128;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Optional;

public class EventIdTypeAndData {

  private Int128 id;
  private String eventType;
  private String eventData;
  private Optional<String> metadata;

  public EventIdTypeAndData() {
  }

  public EventIdTypeAndData(Int128 id, String eventType, String eventData, Optional<String> metadata) {
    this.id = id;
    this.eventType = eventType;
    this.eventData = eventData;
    this.metadata = metadata;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public boolean equals(Object o) {
    return EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public Int128 getId() {
    return id;
  }

  public void setId(Int128 id) {
    this.id = id;
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

  public Optional<String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Optional<String> metadata) {
    this.metadata = metadata;
  }

  public void setEventData(String eventData) {
    this.eventData = eventData;
  }
}
