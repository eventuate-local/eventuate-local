package io.eventuate.common.jdbckafkastore;

import io.eventuate.Aggregate;
import io.eventuate.EntityIdAndType;
import io.eventuate.common.id.Int128;
import io.eventuate.javaclient.commonimpl.*;
import io.eventuate.javaclient.spring.jdbc.AbstractJdbcAggregateCrud;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

/**
 * A JDBC-based aggregate store
 */
public class EventuateLocalAggregateCrud extends AbstractJdbcAggregateCrud {

  private TransactionTemplate transactionTemplate;

  public EventuateLocalAggregateCrud(TransactionTemplate transactionTemplate,
                                     EventuateJdbcAccess eventuateJdbcAccess) {
    super(eventuateJdbcAccess);
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public EntityIdVersionAndEventIds save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options) {
    return transactionTemplate.execute(status ->  super.save(aggregateType, events, options));
  }

  @Override
  public <T extends Aggregate<T>> LoadedEvents find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions) {
    return transactionTemplate.execute(status ->  super.find(aggregateType, entityId, findOptions));
  }

  @Override
  public EntityIdVersionAndEventIds update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions) {
    return transactionTemplate.execute(status ->  super.update(entityIdAndType, entityVersion, events, updateOptions));
  }
}
