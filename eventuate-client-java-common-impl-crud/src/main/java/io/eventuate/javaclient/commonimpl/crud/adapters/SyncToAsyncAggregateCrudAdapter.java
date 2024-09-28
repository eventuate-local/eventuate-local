package io.eventuate.javaclient.commonimpl.crud.adapters;

import io.eventuate.Aggregate;
import io.eventuate.CompletableFutureUtil;
import io.eventuate.EntityIdAndType;
import io.eventuate.common.id.Int128;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrudFindOptions;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrudSaveOptions;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrudUpdateOptions;
import io.eventuate.javaclient.commonimpl.crud.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.commonimpl.crud.LoadedEvents;
import io.eventuate.javaclient.commonimpl.crud.sync.AggregateCrud;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SyncToAsyncAggregateCrudAdapter implements io.eventuate.javaclient.commonimpl.crud.AggregateCrud {

  private AggregateCrud target;

  public SyncToAsyncAggregateCrudAdapter(AggregateCrud target) {
    this.target = target;
  }

  @Override
  public CompletableFuture<EntityIdVersionAndEventIds> save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options) {
    try {
      return CompletableFuture.completedFuture(target.save(aggregateType, events, options));
    } catch (Exception e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }

  @Override
  public <T extends Aggregate<T>> CompletableFuture<LoadedEvents> find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions) {
    try {
      return CompletableFuture.completedFuture(target.find(aggregateType, entityId, findOptions));
    } catch (Exception e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<EntityIdVersionAndEventIds> update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions) {
    try {
      return CompletableFuture.completedFuture(target.update(entityIdAndType, entityVersion, events, updateOptions));
    } catch (Exception e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }

  @Override
  public CompletableFuture<EntityIdVersionAndEventIds> update(EntityIdAndType entityIdAndType, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions) {
    try {
      return CompletableFuture.completedFuture(target.update(entityIdAndType, events, updateOptions));
    } catch (Exception e) {
      return CompletableFutureUtil.failedFuture(e);
    }
  }
}
