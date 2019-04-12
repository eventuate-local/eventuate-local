package io.eventuate.sql.dialect;

import org.springframework.beans.factory.annotation.Value;

public class DefaultEventuateSqlDialect implements EventuateSqlDialect {

  @Value("${eventuate.current.time.in.milliseconds.sql:#{null}}")
  private String customSql;

  @Override
  public boolean supports(String driver) {
    return true;
  }

  @Override
  public String addLimitToSql(String sql, String limitExpression) {
    return String.format("%s limit %s", sql, limitExpression);
  }

  public String getCurrentTimeInMillisecondsExpression() {
    return customSql;
  }
}
