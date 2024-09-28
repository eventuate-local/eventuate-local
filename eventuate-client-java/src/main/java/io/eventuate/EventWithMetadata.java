package io.eventuate;

import io.eventuate.common.id.Int128;

import java.util.Map;
import java.util.Optional;

/**
 * An event with it's id
 */
public class EventWithMetadata {

  private Event event;
  private Int128 id;
  private Optional<Map<String, String>> metadata;

  public EventWithMetadata(Event event, Int128 id, Optional<Map<String, String>> metadata) {
    this.event = event;
    this.id = id;
    this.metadata = metadata;
  }

  public Event getEvent() {
    return event;
  }

  public Int128 getId() {
    return id;
  }

  public Optional<Map<String, String>> getMetadata() {
    return metadata;
  }
}
