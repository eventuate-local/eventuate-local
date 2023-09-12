package io.eventuate;

import io.eventuate.common.id.Int128;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SnapshotManagerImpl implements SnapshotManager {

  private Map<Class<?>, SnapshotStrategy> strategies = new HashMap<>();

  public void addStrategy(SnapshotStrategy snapshotStrategy) {
    strategies.put(snapshotStrategy.getAggregateClass(), snapshotStrategy);
  }

  @Override
  public Optional<Snapshot> possiblySnapshot(Aggregate aggregate, Optional<Int128> snapshotVersion, List<EventWithMetadata> oldEvents, List<Event> newEvents) {
    SnapshotStrategy strategy = strategies.get(aggregate.getClass());
    if (strategy == null)
      return Optional.empty();
    return strategy.possiblySnapshot(aggregate, snapshotVersion, oldEvents, newEvents);
  }

  @Override
  public Aggregate recreateFromSnapshot(Class<?> clasz, Snapshot snapshot, MissingApplyEventMethodStrategy missingApplyEventMethodStrategy) {
    SnapshotStrategy strategy = strategies.get(clasz);
    return strategy.recreateAggregate(clasz, snapshot, missingApplyEventMethodStrategy);
  }
}
