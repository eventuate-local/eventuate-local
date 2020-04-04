package io.eventuate.javaclient.spring.jdbc;

import io.eventuate.common.common.spring.jdbc.EventuateSpringJdbcStatementExecutor;
import io.eventuate.common.common.spring.jdbc.EventuateSpringTransactionTemplate;
import io.eventuate.common.jdbc.*;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccessImpl;
import io.eventuate.javaclient.jdbc.common.tests.CommonEventuateJdbcAccessImplTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

public abstract class EventuateJdbcAccessImplTest extends CommonEventuateJdbcAccessImplTest {

  public static class Config {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventuateTransactionTemplate eventuateTransactionTemplate(TransactionTemplate transactionTemplate) {
      return new EventuateSpringTransactionTemplate(transactionTemplate);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor(JdbcTemplate jdbcTemplate) {
      return new EventuateSpringJdbcStatementExecutor(jdbcTemplate);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventuateCommonJdbcOperations eventuateCommonJdbcOperations(EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor) {
      return new EventuateCommonJdbcOperations(eventuateJdbcStatementExecutor);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventuateJdbcAccess eventuateJdbcAccess(EventuateTransactionTemplate eventuateTransactionTemplate, EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor, EventuateCommonJdbcOperations eventuateCommonJdbcOperations) {
      return new EventuateJdbcAccessImpl(eventuateTransactionTemplate, eventuateJdbcStatementExecutor, eventuateCommonJdbcOperations);
    }


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
      return new JdbcTemplate(dataSource);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventuateJdbcAccess eventuateJdbcAccess(EventuateTransactionTemplate eventuateTransactionTemplate,
                                                   EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                   EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                                   EventuateSchema eventuateSchema) {
      return new EventuateJdbcAccessImpl(eventuateTransactionTemplate, eventuateJdbcStatementExecutor, eventuateCommonJdbcOperations, eventuateSchema);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
      return new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }
  }

  @Autowired
  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;

  @Autowired
  private EventuateJdbcAccess eventuateJdbcAccess;


  @Test
  @Override
  public void testSave() {
    super.testSave();
  }

  @Test
  @Override
  public void testFind() {
    super.testFind();
  }

  @Test
  @Override
  public void testUpdate() {
    super.testUpdate();
  }

  @Override
  protected EventuateJdbcStatementExecutor getEventuateJdbcStatementExecutor() {
    return eventuateJdbcStatementExecutor;
  }

  @Override
  protected EventuateJdbcAccess getEventuateJdbcAccess() {
    return eventuateJdbcAccess;
  }
}
