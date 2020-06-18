package io.eventuate.javaclient.micronaut.jdbc;

import io.micronaut.context.annotation.Factory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Singleton;
import javax.sql.DataSource;

@Factory
public class JdbcTestFactory {
  @Singleton
  public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }
}
