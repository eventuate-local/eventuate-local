package io.eventuate.javaclient.spring.jdbc;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.inmemorydatabase.EventuateDatabaseScriptSupplier;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.common.spring.id.ApplicationIdGeneratorCondition;
import io.eventuate.common.spring.id.IdGeneratorConfiguration;
import io.eventuate.common.spring.inmemorydatabase.EventuateCommonInMemoryDatabaseConfiguration;
import io.eventuate.common.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.common.spring.jdbc.sqldialect.SqlDialectConfiguration;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccessImpl;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrud;
import io.eventuate.javaclient.commonimpl.crud.adapters.SyncToAsyncAggregateCrudAdapter;
import io.eventuate.javaclient.commonimpl.events.AggregateEvents;
import io.eventuate.javaclient.commonimpl.events.adapters.SyncToAsyncAggregateEventsAdapter;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventuateClientScheduler;
import io.eventuate.javaclient.jdbc.EventuateEmbeddedTestAggregateStore;
import io.eventuate.javaclient.jdbc.JdkTimerBasedEventuateClientScheduler;
import io.eventuate.javaclient.spring.common.EventuateCommonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

@Configuration
@EnableTransactionManagement
@Import({EventuateCommonConfiguration.class,
        SqlDialectConfiguration.class,
        EventuateCommonInMemoryDatabaseConfiguration.class,
        EventuateCommonJdbcOperationsConfiguration.class,
        IdGeneratorConfiguration.class})
public class EmbeddedTestAggregateStoreConfiguration {

  @Bean
  @Conditional(ApplicationIdGeneratorCondition.class)
  public EventuateDatabaseScriptSupplier eventuateCommonInMemoryScriptSupplierForEventuateLocal() {
    return () -> Collections.singletonList("eventuate-embedded-schema.sql");
  }

  @Bean
  @ConditionalOnProperty(name = "eventuate.outbox.id")
  public EventuateDatabaseScriptSupplier eventuateCommonInMemoryScriptSupplierForEventuateLocalDbId() {
    return () -> Collections.singletonList("eventuate-embedded-schema-db-id.sql");
  }

  @Bean
  public EventuateJdbcAccess eventuateJdbcAccess(IdGenerator idGenerator,
                                                 EventuateTransactionTemplate eventuateTransactionTemplate,
                                                 EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                 EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                                 EventuateSchema eventuateSchema,
                                                 SqlDialectSelector sqlDialectSelector,
                                                 @Value("${spring.datasource.driver-class-name}") String driver) {

    return new EventuateJdbcAccessImpl(idGenerator,
            eventuateTransactionTemplate,
            eventuateJdbcStatementExecutor,
            eventuateCommonJdbcOperations,
            sqlDialectSelector.getDialect(driver),
            eventuateSchema);
  }

  @Bean
  public EventuateEmbeddedTestAggregateStore eventuateEmbeddedTestAggregateStore(EventuateJdbcAccess eventuateJdbcAccess) {
    return new EventuateEmbeddedTestAggregateStore(eventuateJdbcAccess);
  }

  @Bean
  public AggregateCrud aggregateCrud(io.eventuate.javaclient.commonimpl.crud.sync.AggregateCrud aggregateCrud) {
    return new SyncToAsyncAggregateCrudAdapter(aggregateCrud);
  }

  @Bean
  public AggregateEvents aggregateEvents(io.eventuate.javaclient.commonimpl.events.sync.AggregateEvents aggregateEvents) {
    return new SyncToAsyncAggregateEventsAdapter(aggregateEvents);
  }

  @Bean
  public EventuateClientScheduler eventHandlerRecoveryScheduler() {
    return new JdkTimerBasedEventuateClientScheduler();
  }
}
