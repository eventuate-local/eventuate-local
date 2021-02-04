package io.eventuate.local.java.spring.jdbc.crud;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.common.spring.id.IdGeneratorConfiguration;
import io.eventuate.common.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrud;
import io.eventuate.javaclient.commonimpl.crud.adapters.SyncToAsyncAggregateCrudAdapter;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.spring.common.crud.EventuateCommonCrudConfiguration;
import io.eventuate.local.java.crud.EventuateLocalAggregateCrud;
import io.eventuate.local.java.crud.EventuateLocalJdbcAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableTransactionManagement
@Import({EventuateCommonCrudConfiguration.class, EventuateCommonJdbcOperationsConfiguration.class, IdGeneratorConfiguration.class})
public class EventuateLocalCrudConfiguration {

  @Bean
  public EventuateJdbcAccess eventuateJdbcAccess(IdGenerator idGenerator,
                                                 EventuateTransactionTemplate eventuateTransactionTemplate,
                                                 EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                 EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                                 EventuateSchema eventuateSchema,
                                                 SqlDialectSelector sqlDialectSelector,
                                                 @Value("${spring.datasource.driver-class-name}") String driver) {
    return new EventuateLocalJdbcAccess(idGenerator,
            eventuateTransactionTemplate,
            eventuateJdbcStatementExecutor,
            eventuateCommonJdbcOperations,
            sqlDialectSelector.getDialect(driver),
            eventuateSchema);
  }

  @Bean
  public EventuateLocalAggregateCrud eventuateLocalAggregateCrud(TransactionTemplate transactionTemplate,
                                                                 EventuateJdbcAccess eventuateJdbcAccess) {
    return new EventuateLocalAggregateCrud(transactionTemplate, eventuateJdbcAccess);
  }

  @Bean
  public AggregateCrud asyncAggregateCrud(io.eventuate.javaclient.commonimpl.crud.sync.AggregateCrud aggregateCrud) {
    return new SyncToAsyncAggregateCrudAdapter(aggregateCrud);
  }

  @Bean
  public TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager) {
    return new TransactionTemplate(platformTransactionManager);
  }
}
