package io.eventuate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Map;
import java.util.Optional;

public class UpdateWithoutReadingOptions {

  private final Optional<EventContext> triggeringEvent;
  private final Optional<Map<String, String>> eventMetadata;

  public UpdateWithoutReadingOptions() {
    this.triggeringEvent = Optional.empty();
    this.eventMetadata = Optional.empty();
  }

  public UpdateWithoutReadingOptions(Optional<EventContext> triggeringEvent, Optional<Map<String, String>> eventMetadata) {
    this.triggeringEvent = triggeringEvent;
    this.eventMetadata = eventMetadata;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }

  public Optional<Map<String, String>> getEventMetadata() {
    return eventMetadata;
  }

  public UpdateWithoutReadingOptions withTriggeringEvent(EventContext eventContext) {
    return new UpdateWithoutReadingOptions(Optional.ofNullable(eventContext), this.eventMetadata);
  }

  public UpdateWithoutReadingOptions withEventMetadata(Map<String, String> eventMetadata) {
    return new UpdateWithoutReadingOptions(this.triggeringEvent, Optional.of(eventMetadata));
  }
}
