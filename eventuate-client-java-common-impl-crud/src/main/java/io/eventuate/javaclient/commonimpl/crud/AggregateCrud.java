package io.eventuate.javaclient.commonimpl.crud;

import io.eventuate.*;
import io.eventuate.common.id.Int128;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AggregateCrud {
  CompletableFuture<EntityIdVersionAndEventIds> save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options);

  <T extends Aggregate<T>> CompletableFuture<LoadedEvents> find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions);

  CompletableFuture<EntityIdVersionAndEventIds> update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions);

  CompletableFuture<EntityIdVersionAndEventIds> updateWithoutReading(EntityIdAndType entityIdAndType, List<EventTypeAndData> events, Optional<AggregateCrudUpdateWithoutReadingOptions> updateOptions);
}
