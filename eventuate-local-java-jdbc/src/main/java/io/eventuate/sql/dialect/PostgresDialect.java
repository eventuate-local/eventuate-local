package io.eventuate.sql.dialect;

import io.eventuate.javaclient.spring.jdbc.DefaultEventuateSqlDialect;
import org.springframework.core.Ordered;

public class PostgresDialect extends DefaultEventuateSqlDialect implements Ordered {
  @Override
  public boolean supports(String driver) {
    return "org.postgresql.Driver".equals(driver);
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
