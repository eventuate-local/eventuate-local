package io.eventuate.javaclient.commonimpl.sync;

import io.eventuate.Aggregate;
import io.eventuate.EntityIdAndType;
import io.eventuate.common.id.Int128;
import io.eventuate.javaclient.commonimpl.*;

import java.util.List;
import java.util.Optional;

public interface AggregateCrud {
  EntityIdVersionAndEventIds save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options);

  <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions);

  EntityIdVersionAndEventIds update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions);
}
