package io.eventuate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Map;
import java.util.Optional;

public class UpdateOptions {

  private final Optional<EventContext> triggeringEvent;
  private final Optional<Map<String, String>> eventMetadata;
  private final Optional<Snapshot> snapshot;
  private final Optional<AggregateRepositoryInterceptor> interceptor;

  public UpdateOptions() {
    this.triggeringEvent = Optional.empty();
    this.eventMetadata = Optional.empty();
    this.snapshot = Optional.empty();
    this.interceptor= Optional.empty();
  }

  public UpdateOptions(Optional<EventContext> triggeringEvent, Optional<Map<String, String>> eventMetadata, Optional<Snapshot> snapshot, Optional<AggregateRepositoryInterceptor> interceptor) {
    this.triggeringEvent = triggeringEvent;
    this.eventMetadata = eventMetadata;
    this.snapshot = snapshot;
    this.interceptor= interceptor;
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

  public Optional<Snapshot> getSnapshot() {
    return snapshot;
  }

  public UpdateOptions withTriggeringEvent(EventContext eventContext) {
    return new UpdateOptions(Optional.ofNullable(eventContext), this.eventMetadata, this.snapshot, this.interceptor);
  }

  public UpdateOptions withEventMetadata(Map<String, String> eventMetadata) {
    return new UpdateOptions(this.triggeringEvent, Optional.of(eventMetadata), this.snapshot, this.interceptor);
  }

  public UpdateOptions withSnapshot(Snapshot snapshot) {
    return new UpdateOptions(this.triggeringEvent, this.eventMetadata, Optional.of(snapshot), this.interceptor);
  }

  public UpdateOptions withInterceptor(AggregateRepositoryInterceptor interceptor) {
    return new UpdateOptions(this.triggeringEvent, this.eventMetadata, this.snapshot, Optional.of(interceptor));
  }

  public UpdateOptions withIdempotencyKey(String idempotencyKey) {
    // TODO - implement me
    return this;
  }

  public Optional<AggregateRepositoryInterceptor> getInterceptor() {
    return interceptor;
  }
}
