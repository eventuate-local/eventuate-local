package io.eventuate.javaclient.commonimpl;

import io.eventuate.*;
import io.eventuate.common.id.Int128;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.javaclient.commonimpl.schema.EventuateEventSchemaManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.eventuate.javaclient.commonimpl.AggregateCrudMapping.toEventTypeAndData;
import static io.eventuate.javaclient.commonimpl.EventuateActivity.activityLogger;

public class EventuateAggregateStoreImpl implements EventuateAggregateStore {


  private AggregateCrud aggregateCrud;
  private AggregateEvents aggregateEvents;
  private SnapshotManager snapshotManager;
  private SerializedEventDeserializer serializedEventDeserializer = new DefaultSerializedEventDeserializer();
  private MissingApplyEventMethodStrategy missingApplyEventMethodStrategy;
  private EventuateEventSchemaManager eventuateEventSchemaManager;

  public EventuateAggregateStoreImpl(AggregateCrud aggregateCrud, AggregateEvents aggregateEvents, SnapshotManager snapshotManager, MissingApplyEventMethodStrategy missingApplyEventMethodStrategy,
                                     EventuateEventSchemaManager eventuateEventSchemaManager) {
    this.aggregateCrud = aggregateCrud;
    this.aggregateEvents = aggregateEvents;
    this.snapshotManager = snapshotManager;
    this.missingApplyEventMethodStrategy = missingApplyEventMethodStrategy;
    this.eventuateEventSchemaManager = eventuateEventSchemaManager;
  }

  public void setSerializedEventDeserializer(SerializedEventDeserializer serializedEventDeserializer) {
    this.serializedEventDeserializer = serializedEventDeserializer;
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> save(Class<T> clasz, List<Event> events) {
    return save(clasz, events, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> save(Class<T> clasz, List<Event> events, SaveOptions saveOptions) {
    return save(clasz, events, Optional.ofNullable(saveOptions));
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> save(Class<T> clasz, List<Event> events, Optional<SaveOptions> saveOptions) {
    Optional<String> serializedMetadata = saveOptions.flatMap(so -> withSchemaMetadata(clasz, so.getEventMetadata())).map(JSonMapper::toJson);
    List<EventTypeAndData> serializedEvents = events.stream().map(event -> toEventTypeAndData(event, serializedMetadata)).collect(Collectors.toList());
    CompletableFuture<EntityIdVersionAndEventIds> outcome = aggregateCrud.save(clasz.getName(), serializedEvents, AggregateCrudMapping.toAggregateCrudSaveOptions(saveOptions));
    if (activityLogger.isDebugEnabled())
      return CompletableFutureUtil.tap(outcome, (result, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Saved entity: {} {} {}", clasz.getName(), result.getEntityId(), AggregateCrudMapping.toSerializedEventsWithIds(serializedEvents, result.getEventIds()));
        else
          activityLogger.error(String.format("Save entity failed: %s", clasz.getName()), throwable);
      }).thenApply(EntityIdVersionAndEventIds::toEntityIdAndVersion);
    else
      return outcome.thenApply(EntityIdVersionAndEventIds::toEntityIdAndVersion);
  }

  private Optional<Map<String, String>> withSchemaMetadata(Class clasz, Optional<Map<String, String>> eventMetadata) {
    Map<String, String> schemaMetadata = eventuateEventSchemaManager.currentSchemaMetadata(clasz.getName());
    if (schemaMetadata.isEmpty())
      return eventMetadata;
    Map<String, String> result = eventMetadata.orElseGet(HashMap::new);
    schemaMetadata.forEach(result::putIfAbsent);
    return Optional.of(result);
  }


  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityWithMetadata<T>> find(Class<T> clasz, String entityId) {
    return find(clasz, entityId, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityWithMetadata<T>> find(Class<T> clasz, String entityId, FindOptions findOptions) {
    return find(clasz, entityId, Optional.ofNullable(findOptions));
  }


  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityWithMetadata<T>> find(Class<T> clasz, String entityId, Optional<FindOptions> findOptions) {
    CompletableFuture<LoadedEvents> outcome = aggregateCrud.find(clasz.getName(), entityId, AggregateCrudMapping.toAggregateCrudFindOptions(findOptions));

    CompletableFuture<LoadedEvents> tappedOutcome;
    if (activityLogger.isDebugEnabled())
      tappedOutcome = CompletableFutureUtil.tap(outcome, (result, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Loaded entity: {} {} {}", clasz.getName(), entityId, result.getEvents());
        else {
          if (throwable instanceof EventuateException)
            activityLogger.trace(String.format("Find entity failed: %s %s %s", clasz.getName(), entityId, throwable.getClass().getName()));
          else
            activityLogger.trace(String.format("Find entity failed: %s %s", clasz.getName(), entityId), throwable);
        }
      });
    else
      tappedOutcome = outcome;

    return tappedOutcome.thenApply(le -> {
      List<EventWithMetadata> eventsWithIds = eventuateEventSchemaManager.upcastEvents(clasz.getName(), le.getEvents()).stream().map(AggregateCrudMapping::toEventWithMetadata).collect(Collectors.toList());
      List<Event> events = eventsWithIds.stream().map(EventWithMetadata::getEvent).collect(Collectors.toList());
      return new EntityWithMetadata<T>(
              new EntityIdAndVersion(entityId, le.getEvents().isEmpty() ? le.getSnapshot().get().getEntityVersion() : le.getEvents().get(le.getEvents().size() - 1).getId()),
              le.getSnapshot().map(SerializedSnapshotWithVersion::getEntityVersion),
              eventsWithIds,
              le.getSnapshot().map(ss ->
                      Aggregates.applyEventsToMutableAggregate((T) snapshotManager.recreateFromSnapshot(clasz, AggregateCrudMapping.toSnapshot(ss.getSerializedSnapshot()), missingApplyEventMethodStrategy), events, missingApplyEventMethodStrategy))
                      .orElseGet(() -> Aggregates.recreateAggregate(clasz, events, missingApplyEventMethodStrategy)));
    });
  }

  //     T aggregate = snapshot.map(ss -> newAggregateFromSnapshot(clasz, ss)).orElseGet(() -> newAggregate(clasz));

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events) {
    return update(clasz, entityIdAndVersion, events, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, UpdateOptions updateOptions) {
    return update(clasz, entityIdAndVersion, events, Optional.ofNullable(updateOptions));
  }


  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, Optional<UpdateOptions> updateOptions) {
    Optional<String> serializedMetadata = updateOptions.flatMap(so -> withSchemaMetadata(clasz, so.getEventMetadata())).map(JSonMapper::toJson);
    List<EventTypeAndData> serializedEvents = events.stream().map(event -> toEventTypeAndData(event, serializedMetadata)).collect(Collectors.toList());

    CompletableFuture<EntityIdVersionAndEventIds> outcome = aggregateCrud.update(new EntityIdAndType(entityIdAndVersion.getEntityId(), clasz.getName()),
            entityIdAndVersion.getEntityVersion(),
            serializedEvents,
            AggregateCrudMapping.toAggregateCrudUpdateOptions(updateOptions));
    if (activityLogger.isDebugEnabled())
      return CompletableFutureUtil.tap(outcome, (result, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Updated entity: {} {} {}", clasz.getName(), result.getEntityId(), AggregateCrudMapping.toSerializedEventsWithIds(serializedEvents, result.getEventIds()));
        else
          activityLogger.error(String.format("Update entity failed: %s %s", clasz.getName(), entityIdAndVersion), throwable);
      }).thenApply(EntityIdVersionAndEventIds::toEntityIdAndVersion);
    else
      return outcome.thenApply(EntityIdVersionAndEventIds::toEntityIdAndVersion);
  }

  @Override
  public CompletableFuture<?> subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<DispatchedEvent<Event>, CompletableFuture<?>> handler) {
    if (activityLogger.isDebugEnabled())
      activityLogger.debug("Subscribing {} {}", subscriberId, aggregatesAndEvents);
    CompletableFuture<?> outcome = aggregateEvents.subscribe(subscriberId, aggregatesAndEvents, subscriberOptions,
            se -> serializedEventDeserializer.toDispatchedEvent(eventuateEventSchemaManager.upcastEvent(se)).map(handler::apply).orElse(CompletableFuture.completedFuture(null)));
    if (activityLogger.isDebugEnabled())
      return CompletableFutureUtil.tap(outcome, (result, throwable) -> {
        if (throwable == null)
          activityLogger.debug("Subscribed {} {}", subscriberId, aggregatesAndEvents);
        else
          activityLogger.error(String.format("Subscribe failed: %s %s", subscriberId, aggregatesAndEvents), throwable);
      });
    else
      return outcome;
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
