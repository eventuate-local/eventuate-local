package io.eventuate.local.java.crud;

import io.eventuate.Aggregate;
import io.eventuate.EntityIdAndType;
import io.eventuate.common.id.Int128;
import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.crud.*;
import io.eventuate.javaclient.jdbc.AbstractJdbcAggregateCrud;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccess;

import java.util.List;
import java.util.Optional;

/**
 * A JDBC-based aggregate store
 */
public class EventuateLocalAggregateCrud extends AbstractJdbcAggregateCrud {

  private EventuateTransactionTemplate transactionTemplate;

  public EventuateLocalAggregateCrud(EventuateTransactionTemplate transactionTemplate,
                                     EventuateJdbcAccess eventuateJdbcAccess) {
    super(eventuateJdbcAccess);
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public EntityIdVersionAndEventIds save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options) {
    return transactionTemplate.executeInTransaction(() ->  super.save(aggregateType, events, options));
  }

  @Override
  public <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions) {
    return transactionTemplate.executeInTransaction(() ->  super.find(aggregateType, entityId, findOptions));
  }

  @Override
  public EntityIdVersionAndEventIds update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions) {
    return transactionTemplate.executeInTransaction(() ->  super.update(entityIdAndType, entityVersion, events, updateOptions));
  }
}
