package io.eventuate.sql.dialect;

import io.eventuate.javaclient.spring.jdbc.DefaultEventuateSqlDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SqlDialectConfiguration {

  @Bean
  public MySqlDialect mySqlDialect() {
    return new MySqlDialect();
  }

  @Bean
  public PostgresDialect postgreSQLDialect() {
    return new PostgresDialect();
  }

  @Bean
  public MsSqlDialect msSqlDialect() {
    return new MsSqlDialect();
  }

  @Bean
  public DefaultEventuateSqlDialect defaultSqlDialect() {
    return new DefaultEventuateSqlDialect ();
  }

  @Bean
  public SqlDialectSelector sqlDialectSelector(@Value("${spring.datasource.driver-class-name}") String driver) {
    return new SqlDialectSelector(driver);
  }
}
