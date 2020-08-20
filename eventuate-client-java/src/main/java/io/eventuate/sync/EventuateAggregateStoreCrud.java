package io.eventuate.sync;

import io.eventuate.*;
import io.eventuate.common.id.Int128;

import java.util.List;
import java.util.Optional;

public interface EventuateAggregateStoreCrud {
  <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> clasz, List<Event> events);

  <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> clasz, List<Event> events, SaveOptions saveOptions);

  <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> clasz, List<Event> events, Optional<SaveOptions> saveOptions);

  <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId);

  <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId, FindOptions findOptions);

  <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId, Optional<FindOptions> findOptions);

  <T extends Aggregate<T>> EntityIdAndVersion update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events);

  <T extends Aggregate<T>> EntityIdAndVersion update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, UpdateOptions updateOptions);

  <T extends Aggregate<T>> EntityIdAndVersion update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, Optional<UpdateOptions> updateOptions);

  <T extends Aggregate<T>> EntityIdAndVersion updateWithoutReading(Class<T> clasz, String entityId, List<Event> events);

  <T extends Aggregate<T>> EntityIdAndVersion updateWithoutReading(Class<T> clasz, String entityId, List<Event> events, UpdateWithoutReadingOptions updateOptions);

  <T extends Aggregate<T>> EntityIdAndVersion updateWithoutReading(Class<T> clasz, String entityId, List<Event> events, Optional<UpdateWithoutReadingOptions> updateOptions);

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
   * @return the aggregate
   */
  Aggregate recreateFromSnapshot(Class<?> clasz, Snapshot snapshot);
}
