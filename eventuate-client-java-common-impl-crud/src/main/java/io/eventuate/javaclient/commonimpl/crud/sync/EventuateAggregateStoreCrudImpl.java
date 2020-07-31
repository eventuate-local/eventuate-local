package io.eventuate.javaclient.commonimpl.crud.sync;

import io.eventuate.*;
import io.eventuate.common.id.Int128;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.crud.*;
import io.eventuate.sync.EventuateAggregateStoreCrud;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.eventuate.javaclient.commonimpl.crud.AggregateCrudMapping.*;
import static io.eventuate.javaclient.commonimpl.common.EventuateActivity.activityLogger;

public class EventuateAggregateStoreCrudImpl implements EventuateAggregateStoreCrud {

  private AggregateCrud aggregateCrud;
  private SnapshotManager snapshotManager;
  private MissingApplyEventMethodStrategy missingApplyEventMethodStrategy;

  public EventuateAggregateStoreCrudImpl(AggregateCrud aggregateCrud,
                                         SnapshotManager snapshotManager,
                                         MissingApplyEventMethodStrategy missingApplyEventMethodStrategy) {
    this.aggregateCrud = aggregateCrud;
    this.snapshotManager = snapshotManager;
    this.missingApplyEventMethodStrategy = missingApplyEventMethodStrategy;
  }

  @Override
  public <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> clasz, List<Event> events) {
    return save(clasz, events, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> clasz, List<Event> events, SaveOptions saveOptions) {
    return save(clasz, events, Optional.ofNullable(saveOptions));
  }

  @Override
  public <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> clasz, List<Event> events, Optional<SaveOptions> saveOptions) {
    Optional<String> serializedMetadata = saveOptions.flatMap(SaveOptions::getEventMetadata).map(JSonMapper::toJson);
    List<EventTypeAndData> serializedEvents = events.stream().map(event -> toEventTypeAndData(event, serializedMetadata)).collect(Collectors.toList());
    try {
      EntityIdVersionAndEventIds result = aggregateCrud.save(clasz.getName(), serializedEvents, toAggregateCrudSaveOptions(saveOptions));
      if (activityLogger.isDebugEnabled())
        activityLogger.debug("Saved entity: {} {} {}", clasz.getName(), result.getEntityId(), toSerializedEventsWithIds(serializedEvents, result.getEventIds()));
      return result.toEntityIdAndVersion();
    } catch (RuntimeException e) {
      activityLogger.error(String.format("Save entity failed: %s", clasz.getName()), e);
      throw e;
    }
  }


  @Override
  public <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId) {
    return find(clasz, entityId, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId, FindOptions findOptions) {
    return find(clasz, entityId, Optional.ofNullable(findOptions));
  }

  @Override
  public <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId, Optional<FindOptions> findOptions) {
    try {
      LoadedEvents le = aggregateCrud.find(clasz.getName(), entityId, toAggregateCrudFindOptions(findOptions));
      if (activityLogger.isDebugEnabled())
        activityLogger.debug("Loaded entity: {} {} {}", clasz.getName(), entityId, le.getEvents());
      List<EventWithMetadata> eventsWithIds = le.getEvents().stream().map(AggregateCrudMapping::toEventWithMetadata).collect(Collectors.toList());
      List<Event> events = eventsWithIds.stream().map(EventWithMetadata::getEvent).collect(Collectors.toList());
      return new EntityWithMetadata<T>(
              new EntityIdAndVersion(entityId,
                      le.getEvents().isEmpty() ? le.getSnapshot().get().getEntityVersion() : le.getEvents().get(le.getEvents().size() - 1).getId()),
              le.getSnapshot().map(SerializedSnapshotWithVersion::getEntityVersion),
              eventsWithIds,
              le.getSnapshot().map(ss ->
                      Aggregates.applyEventsToMutableAggregate((T) snapshotManager.recreateFromSnapshot(clasz, AggregateCrudMapping.toSnapshot(ss.getSerializedSnapshot()), missingApplyEventMethodStrategy), events, missingApplyEventMethodStrategy))
                      .orElseGet(() -> Aggregates.recreateAggregate(clasz, events, missingApplyEventMethodStrategy)));
    } catch (RuntimeException e) {
      if (activityLogger.isDebugEnabled())
        activityLogger.trace(String.format("Find entity failed: %s %s", clasz.getName(), entityId), e);
      throw e;
    }
  }

  @Override
  public <T extends Aggregate<T>> EntityIdAndVersion update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events) {
    return update(clasz, entityIdAndVersion, events, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> EntityIdAndVersion update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, UpdateOptions updateOptions) {
    return update(clasz, entityIdAndVersion, events, Optional.ofNullable(updateOptions));
  }


  @Override
  public <T extends Aggregate<T>> EntityIdAndVersion update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, Optional<UpdateOptions> updateOptions) {
    try {
      Optional<String> serializedEventMetadata = updateOptions.flatMap(UpdateOptions::getEventMetadata).map(JSonMapper::toJson);
      List<EventTypeAndData> serializedEvents = events.stream().map(event -> toEventTypeAndData(event, serializedEventMetadata)).collect(Collectors.toList());
      EntityIdVersionAndEventIds result = aggregateCrud.update(new EntityIdAndType(entityIdAndVersion.getEntityId(), clasz.getName()),
              entityIdAndVersion.getEntityVersion(),
              serializedEvents,
              toAggregateCrudUpdateOptions(updateOptions));
      if (activityLogger.isDebugEnabled())
        activityLogger.debug("Updated entity: {} {} {}", clasz.getName(), result.getEntityId(), toSerializedEventsWithIds(serializedEvents, result.getEventIds()));

      return result.toEntityIdAndVersion();
    } catch (RuntimeException e) {
      if (activityLogger.isDebugEnabled())
        activityLogger.error(String.format("Update entity failed: %s %s", clasz.getName(), entityIdAndVersion), e);
      throw e;
    }
  }

  @Override
  public Optional<Snapshot> possiblySnapshot(Aggregate aggregate, Optional<Int128> snapshotVersion, List<EventWithMetadata> oldEvents, List<Event> newEvents) {
    return snapshotManager.possiblySnapshot(aggregate, snapshotVersion, oldEvents, newEvents);
  }

  @Override
  public Aggregate recreateFromSnapshot(Class<?> clasz, Snapshot snapshot) {
    return snapshotManager.recreateFromSnapshot(clasz, snapshot, missingApplyEventMethodStrategy);
  }
}
