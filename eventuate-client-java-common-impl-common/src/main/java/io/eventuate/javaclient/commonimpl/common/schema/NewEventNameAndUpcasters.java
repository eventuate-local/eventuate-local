package io.eventuate.javaclient.commonimpl.common.schema;

import java.util.List;
import java.util.Optional;

public class NewEventNameAndUpcasters  {
  private final Optional<String> eventType;
  private final List<EventUpcaster> upcasters;

  public NewEventNameAndUpcasters(Optional<String> eventType, List<EventUpcaster> upcasters) {
    this.eventType = eventType;
    this.upcasters = upcasters;
  }

  public Optional<String> getEventType() {
    return eventType;
  }

  public List<EventUpcaster> getUpcasters() {
    return upcasters;
  }

  public boolean isEmpty() {
    return !eventType.isPresent() && upcasters.isEmpty();
  }
}
