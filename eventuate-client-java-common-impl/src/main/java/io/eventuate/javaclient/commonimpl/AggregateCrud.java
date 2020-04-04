package io.eventuate.javaclient.commonimpl;

import io.eventuate.*;
import io.eventuate.common.id.Int128;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AggregateCrud {
  CompletableFuture<EntityIdVersionAndEventIds> save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options);

  <T extends Aggregate<T>> CompletableFuture<LoadedEvents> find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions);

  CompletableFuture<EntityIdVersionAndEventIds> update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions);
}
