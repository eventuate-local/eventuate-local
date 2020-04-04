package io.eventuate.javaclient.commonimpl;

import io.eventuate.EventContext;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Optional;

public class AggregateCrudFindOptions {

  private Optional<EventContext> triggeringEvent = Optional.empty();

  public AggregateCrudFindOptions() {
  }

  public AggregateCrudFindOptions(Optional<EventContext> triggeringEvent) {
    this.triggeringEvent = triggeringEvent;
  }

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

  public AggregateCrudFindOptions withTriggeringEvent(EventContext eventContext) {
    this.triggeringEvent = Optional.ofNullable(eventContext);
    return this;
  }

  public AggregateCrudFindOptions withTriggeringEvent(Optional<EventContext> eventContext) {
    this.triggeringEvent = eventContext;
    return this;
  }
}
