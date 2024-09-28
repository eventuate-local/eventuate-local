package io.eventuate.javaclient.spring.jdbc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@Import(EmbeddedTestAggregateStoreConfiguration.class)
public class CommonEventuateJdbcAccessImplTestConfiguration {

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public TransactionTemplate transactionTemplate(DataSource dataSource) {
    return new TransactionTemplate(new DataSourceTransactionManager(dataSource));
  }
}
