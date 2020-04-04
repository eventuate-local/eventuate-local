package io.eventuate;

import io.eventuate.common.id.Int128;

import java.util.List;
import java.util.Optional;

/**
 * A strategy for creating snapshots of a
 */
public interface SnapshotStrategy {

  /**
   * The aggregate class that this is a strategy for
   *
   * @return the aggregate class
   */
  Class<?> getAggregateClass();

  /**
   * Possibly generate a snapshot
   *
   * @param aggregate - the updated aggregate
   * @param snapshotVersion - the version of the snapshot, if any, that the aggregate was created from
   * @param oldEvents - the old events that were used to recreate the aggregate
   * @param newEvents - the new events generated as a result of executing a command
   * @return an optional snapshot
   */
  Optional<Snapshot> possiblySnapshot(Aggregate aggregate, Optional<Int128> snapshotVersion, List<EventWithMetadata> oldEvents, List<Event> newEvents);

  /**
   * Recreate an aggregate from a snapshot
   *
   * @param clasz the aggregate class
   * @param snapshot the snapshot
   * @param missingApplyEventMethodStrategy
   * @return the aggregate
   */
  Aggregate recreateAggregate(Class<?> clasz, Snapshot snapshot, MissingApplyEventMethodStrategy missingApplyEventMethodStrategy);
}
