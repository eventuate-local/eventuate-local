package io.eventuate.javaclient.commonimpl.crud;

import io.eventuate.javaclient.commonimpl.common.EventIdTypeAndData;

import java.util.List;
import java.util.Optional;

public class LoadedEvents {

  private Optional<SerializedSnapshotWithVersion> snapshot;
  private List<EventIdTypeAndData> events;

  public LoadedEvents(Optional<SerializedSnapshotWithVersion> snapshot, List<EventIdTypeAndData> events) {
    this.snapshot = snapshot;
    this.events = events;
  }

  public Optional<SerializedSnapshotWithVersion> getSnapshot() {
    return snapshot;
  }

  public List<EventIdTypeAndData> getEvents() {
    return events;
  }
}
