package io.eventuate;

import io.eventuate.common.id.Int128;

import java.util.List;
import java.util.Optional;

public interface SnapshotManager {
  Optional<Snapshot> possiblySnapshot(Aggregate aggregate, Optional<Int128> snapshotVersion, List<EventWithMetadata> oldEvents, List<Event> newEvents);
  Aggregate recreateFromSnapshot(Class<?> clasz, Snapshot snapshot, MissingApplyEventMethodStrategy missingApplyEventMethodStrategy);
}
