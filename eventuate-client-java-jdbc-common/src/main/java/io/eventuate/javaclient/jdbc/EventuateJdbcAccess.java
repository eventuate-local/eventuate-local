package io.eventuate.javaclient.jdbc;

import io.eventuate.Aggregate;
import io.eventuate.EntityIdAndType;
import io.eventuate.common.id.Int128;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.crud.*;

import java.util.List;
import java.util.Optional;

public interface EventuateJdbcAccess {

  SaveUpdateResult save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> saveOptions);

  <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions);

  SaveUpdateResult update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions);

  SaveUpdateResult update(EntityIdAndType entityIdAndType, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions);
}
