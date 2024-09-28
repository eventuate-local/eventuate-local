package io.eventuate.javaclient.micronaut.jdbc;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.inmemorydatabase.EventuateDatabaseScriptSupplier;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccessImpl;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrud;
import io.eventuate.javaclient.commonimpl.crud.adapters.SyncToAsyncAggregateCrudAdapter;
import io.eventuate.javaclient.commonimpl.events.AggregateEvents;
import io.eventuate.javaclient.commonimpl.events.adapters.SyncToAsyncAggregateEventsAdapter;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventuateClientScheduler;
import io.eventuate.javaclient.jdbc.EventuateEmbeddedTestAggregateStore;
import io.eventuate.javaclient.jdbc.JdkTimerBasedEventuateClientScheduler;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;

import javax.inject.Singleton;
import java.util.Collections;

@Factory
public class EmbeddedTestAggregateStoreFactory {

  @Singleton
  @Requires(missingProperty = "eventuate.outbox.id")
  public EventuateDatabaseScriptSupplier eventuateCommonInMemoryScriptSupplierForEventuateLocal() {
    return () -> Collections.singletonList("eventuate-embedded-schema.sql");
  }

  @Singleton
  @Requires(property = "eventuate.outbox.id")
  public EventuateDatabaseScriptSupplier eventuateCommonInMemoryScriptSupplierForEventuateLocalDbId() {
    return () -> Collections.singletonList("eventuate-embedded-schema-db-id.sql");
  }

  @Singleton
  @Primary
  public EventuateJdbcAccess eventuateJdbcAccess(IdGenerator idGenerator,
                                                 EventuateTransactionTemplate eventuateTransactionTemplate,
                                                 EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                 EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                                 EventuateSchema eventuateSchema,
                                                 SqlDialectSelector sqlDialectSelector,
                                                 @Value("${datasources.default.driver-class-name}") String driver) {

    return new EventuateJdbcAccessImpl(idGenerator,
            eventuateTransactionTemplate,
            eventuateJdbcStatementExecutor,
            eventuateCommonJdbcOperations,
            sqlDialectSelector.getDialect(driver),
            eventuateSchema);
  }

  @Singleton
  @Primary
  public EventuateEmbeddedTestAggregateStore eventuateEmbeddedTestAggregateStore(EventuateJdbcAccess eventuateJdbcAccess) {
    return new EventuateEmbeddedTestAggregateStore(eventuateJdbcAccess);
  }

  @Singleton
  @Primary
  public AggregateCrud aggregateCrud(io.eventuate.javaclient.commonimpl.crud.sync.AggregateCrud aggregateCrud) {
    return new SyncToAsyncAggregateCrudAdapter(aggregateCrud);
  }

  @Singleton
  public AggregateEvents aggregateEvents(io.eventuate.javaclient.commonimpl.events.sync.AggregateEvents aggregateEvents) {
    return new SyncToAsyncAggregateEventsAdapter(aggregateEvents);
  }

  @Singleton
  public EventuateClientScheduler eventHandlerRecoveryScheduler() {
    return new JdkTimerBasedEventuateClientScheduler();
  }
}
