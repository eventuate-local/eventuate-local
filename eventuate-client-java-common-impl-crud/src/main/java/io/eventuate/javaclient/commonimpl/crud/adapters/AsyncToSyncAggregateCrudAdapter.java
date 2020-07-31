package io.eventuate.javaclient.commonimpl.crud.adapters;

import io.eventuate.Aggregate;
import io.eventuate.CompletableFutureUtil;
import io.eventuate.EntityIdAndType;
import io.eventuate.common.id.Int128;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.common.adapters.AsyncToSyncTimeoutOptions;
import io.eventuate.javaclient.commonimpl.crud.*;

import java.util.List;
import java.util.Optional;

public class AsyncToSyncAggregateCrudAdapter implements io.eventuate.javaclient.commonimpl.crud.sync.AggregateCrud {

  private AggregateCrud target;

  private AsyncToSyncTimeoutOptions timeoutOptions = new AsyncToSyncTimeoutOptions();

  public AsyncToSyncAggregateCrudAdapter(AggregateCrud target) {
    this.target = target;
  }

  @Override
  public EntityIdVersionAndEventIds save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options) {
    try {
      return target.save(aggregateType, events, options).get(timeoutOptions.getTimeout(), timeoutOptions.getTimeUnit());
    } catch (Throwable e) {
      Throwable unwrapped = CompletableFutureUtil.unwrap(e);
      if (unwrapped instanceof RuntimeException)
        throw (RuntimeException)unwrapped;
      else
        throw new RuntimeException(unwrapped);
    }
  }

  @Override
  public <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions) {
    try {
      return target.find(aggregateType, entityId, findOptions).get(timeoutOptions.getTimeout(), timeoutOptions.getTimeUnit());
    } catch (Throwable e) {
      Throwable unwrapped = CompletableFutureUtil.unwrap(e);
      if (unwrapped instanceof RuntimeException)
        throw (RuntimeException)unwrapped;
      else
        throw new RuntimeException(unwrapped);
    }
  }

  @Override
  public EntityIdVersionAndEventIds update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions) {
    try {
      return target.update(entityIdAndType, entityVersion, events, updateOptions).get(timeoutOptions.getTimeout(), timeoutOptions.getTimeUnit());
    } catch (Throwable e) {
      Throwable unwrapped = CompletableFutureUtil.unwrap(e);
      if (unwrapped instanceof RuntimeException)
        throw (RuntimeException)unwrapped;
      else
        throw new RuntimeException(unwrapped);
    }
  }

  public void setTimeoutOptions(AsyncToSyncTimeoutOptions timeoutOptions) {
    this.timeoutOptions = timeoutOptions;
  }
}
