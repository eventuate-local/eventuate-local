package io.eventuate.javaclient.commonimpl;

import io.eventuate.*;
import io.eventuate.common.id.Int128;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EventuateAggregateStoreImpl implements EventuateAggregateStore {

  private EventuateAggregateStoreCrud eventuateAggregateStoreCrud;
  private EventuateAggregateStoreEvents eventuateAggregateStoreEvents;

  public EventuateAggregateStoreImpl(EventuateAggregateStoreCrud eventuateAggregateStoreCrud, EventuateAggregateStoreEvents eventuateAggregateStoreEvents) {
    this.eventuateAggregateStoreCrud = eventuateAggregateStoreCrud;
    this.eventuateAggregateStoreEvents = eventuateAggregateStoreEvents;
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> save(Class<T> clasz, List<Event> events) {
    return eventuateAggregateStoreCrud.save(clasz, events);
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> save(Class<T> clasz, List<Event> events, SaveOptions saveOptions) {
    return eventuateAggregateStoreCrud.save(clasz, events, saveOptions);
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> save(Class<T> clasz, List<Event> events, Optional<SaveOptions> saveOptions) {
    return eventuateAggregateStoreCrud.save(clasz, events, saveOptions);
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityWithMetadata<T>> find(Class<T> clasz, String entityId) {
    return eventuateAggregateStoreCrud.find(clasz, entityId);
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityWithMetadata<T>> find(Class<T> clasz, String entityId, FindOptions findOptions) {
    return eventuateAggregateStoreCrud.find(clasz, entityId, findOptions);
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityWithMetadata<T>> find(Class<T> clasz, String entityId, Optional<FindOptions> findOptions) {
    return eventuateAggregateStoreCrud.find(clasz, entityId, findOptions);
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events) {
    return eventuateAggregateStoreCrud.update(clasz, entityIdAndVersion, events);
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, UpdateOptions updateOptions) {
    return eventuateAggregateStoreCrud.update(clasz, entityIdAndVersion, events, updateOptions);
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, EntityIdAndVersion entityIdAndVersion, List<Event> events, Optional<UpdateOptions> updateOptions) {
    return eventuateAggregateStoreCrud.update(clasz, entityIdAndVersion, events, updateOptions);
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, String entityId, List<Event> events) {
    return eventuateAggregateStoreCrud.update(clasz, entityId, events, Optional.empty());
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, String entityId, List<Event> events, UpdateOptions updateOptions) {
    return eventuateAggregateStoreCrud.update(clasz, entityId, events, Optional.of(updateOptions));
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<EntityIdAndVersion> update(Class<T> clasz, String entityId, List<Event> events, Optional<UpdateOptions> updateOptions) {
    return eventuateAggregateStoreCrud.update(clasz, entityId, events, updateOptions);
  }

  @Override
  public Optional<Snapshot> possiblySnapshot(Aggregate aggregate, Optional<Int128> snapshotVersion, List<EventWithMetadata> oldEvents, List<Event> newEvents) {
    return eventuateAggregateStoreCrud.possiblySnapshot(aggregate, snapshotVersion, oldEvents, newEvents);
  }

  @Override
  public Aggregate recreateFromSnapshot(Class<?> clasz, Snapshot snapshot) {
    return eventuateAggregateStoreCrud.recreateFromSnapshot(clasz, snapshot);
  }

  @Override
  public CompletableFuture<?> subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions subscriberOptions, Function<DispatchedEvent<Event>, CompletableFuture<?>> dispatch) {
    return eventuateAggregateStoreEvents.subscribe(subscriberId, aggregatesAndEvents, subscriberOptions, dispatch);
  }
}
