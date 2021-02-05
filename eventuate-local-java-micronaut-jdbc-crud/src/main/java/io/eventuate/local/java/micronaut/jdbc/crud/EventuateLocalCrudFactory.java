package io.eventuate.local.java.micronaut.jdbc.crud;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccess;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrud;
import io.eventuate.javaclient.commonimpl.crud.adapters.SyncToAsyncAggregateCrudAdapter;
import io.eventuate.local.java.crud.EventuateLocalAggregateCrud;
import io.eventuate.local.java.crud.EventuateLocalJdbcAccess;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.function.Supplier;

@Factory
public class EventuateLocalCrudFactory {

  @Singleton
  public EventuateJdbcAccess eventuateJdbcAccess(IdGenerator idGenerator,
                                                 EventuateTransactionTemplate eventuateTransactionTemplate,
                                                 EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                 EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                                 EventuateSchema eventuateSchema,
                                                 SqlDialectSelector sqlDialectSelector,
                                                 @Value("${datasources.default.driver-class-name}") String driver) {
    return new EventuateLocalJdbcAccess(idGenerator,
            eventuateTransactionTemplate,
            eventuateJdbcStatementExecutor,
            eventuateCommonJdbcOperations,
            sqlDialectSelector.getDialect(driver),
            eventuateSchema);
  }

  @Singleton
  public EventuateLocalAggregateCrud eventuateLocalAggregateCrud(EventuateTransactionTemplate transactionTemplate,
                                                                 EventuateJdbcAccess eventuateJdbcAccess) {
    return new EventuateLocalAggregateCrud(transactionTemplate, eventuateJdbcAccess);
  }

  @Singleton
  public AggregateCrud asyncAggregateCrud(io.eventuate.javaclient.commonimpl.crud.sync.AggregateCrud aggregateCrud) {
    return new SyncToAsyncAggregateCrudAdapter(aggregateCrud);
  }

  @Singleton
  @Primary
  public EventuateTransactionTemplate eventuateTransactionTemplate(TransactionTemplate transactionTemplate) {
    return new EventuateTransactionTemplate() {
      @Override
      public <T> T executeInTransaction(Supplier<T> supplier) {
        return transactionTemplate.execute(status -> supplier.get());
      }
    };
  }

  @Singleton
  public TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager) {
    return new TransactionTemplate(platformTransactionManager);
  }

  @Singleton
  @Requires(missingBeans = PlatformTransactionManager.class)
  public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }
}
