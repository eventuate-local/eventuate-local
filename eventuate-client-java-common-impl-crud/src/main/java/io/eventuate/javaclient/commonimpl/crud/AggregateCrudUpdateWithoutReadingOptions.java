package io.eventuate.javaclient.commonimpl.crud;

import io.eventuate.EventContext;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Optional;

public class AggregateCrudUpdateWithoutReadingOptions {

  private final Optional<EventContext> triggeringEvent;

  public AggregateCrudUpdateWithoutReadingOptions() {
    this.triggeringEvent = Optional.empty();
  }

  public AggregateCrudUpdateWithoutReadingOptions(Optional<EventContext> triggeringEvent) {
    this.triggeringEvent = triggeringEvent;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }

  public AggregateCrudUpdateWithoutReadingOptions withTriggeringEvent(EventContext eventContext) {
    return new AggregateCrudUpdateWithoutReadingOptions(Optional.of(eventContext));
  }
}
