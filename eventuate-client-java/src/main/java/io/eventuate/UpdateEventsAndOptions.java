package io.eventuate;

import java.util.List;
import java.util.Optional;

public class UpdateEventsAndOptions {
  private final List<Event> events;
  private final Optional<UpdateOptions> options;

  public UpdateEventsAndOptions(List<Event> events, Optional<UpdateOptions> options) {
    this.events = events;
    this.options = options;
  }

  public Optional<UpdateOptions> getOptions() {
    return options;
  }

  public List<Event> getEvents() {
    return events;
  }
}
