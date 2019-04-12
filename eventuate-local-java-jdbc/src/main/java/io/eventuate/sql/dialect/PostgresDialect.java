package io.eventuate.sql.dialect;

import org.springframework.core.Ordered;

public class PostgresDialect extends DefaultEventuateSqlDialect implements Ordered {
  @Override
  public boolean supports(String driver) {
    return "org.postgresql.Driver".equals(driver);
  }

  @Override
  public String getCurrentTimeInMillisecondsExpression() {
    return "(ROUND(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000))";
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
