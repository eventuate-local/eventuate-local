package io.eventuate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Optional;

public class FindOptions {

  private Optional<EventContext> triggeringEvent = Optional.empty();

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public FindOptions withTriggeringEvent(EventContext eventContext) {
    this.triggeringEvent = Optional.ofNullable(eventContext);
    return this;
  }

  public FindOptions withTriggeringEvent(Optional<EventContext> eventContext) {
    this.triggeringEvent = eventContext;
    return this;
  }
}
