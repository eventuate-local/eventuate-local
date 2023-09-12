package io.eventuate.javaclient.commonimpl.crud;

import io.eventuate.EventContext;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Optional;

public class AggregateCrudUpdateOptions {

  private final Optional<EventContext> triggeringEvent;
  private final Optional<SerializedSnapshot> snapshot;

  public AggregateCrudUpdateOptions() {
    this.triggeringEvent = Optional.empty();
    this.snapshot = Optional.empty();
  }

  public AggregateCrudUpdateOptions(Optional<EventContext> triggeringEvent, Optional<SerializedSnapshot> snapshot) {
    this.triggeringEvent = triggeringEvent;
    this.snapshot = snapshot;
  }

  public AggregateCrudUpdateOptions withSnapshot(SerializedSnapshot serializedSnapshot) {
    return new AggregateCrudUpdateOptions(this.triggeringEvent, Optional.of(serializedSnapshot));
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public Optional<EventContext> getTriggeringEvent() {
    return triggeringEvent;
  }

  public Optional<SerializedSnapshot> getSnapshot() {
    return snapshot;
  }

  public AggregateCrudUpdateOptions withTriggeringEvent(EventContext eventContext) {
    return new AggregateCrudUpdateOptions(Optional.of(eventContext), this.snapshot);
  }

}
