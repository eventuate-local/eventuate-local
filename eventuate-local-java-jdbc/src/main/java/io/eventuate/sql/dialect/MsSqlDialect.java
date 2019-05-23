package io.eventuate.sql.dialect;

import org.springframework.core.Ordered;

public class MsSqlDialect implements EventuateSqlDialect, Ordered {
  @Override
  public String addLimitToSql(String sql, String limitExpression) {
    return sql.replaceFirst("select", String.format("select top %s", limitExpression));
  }

  @Override
  public String getCurrentTimeInMillisecondsExpression() {
    return "(SELECT DATEDIFF_BIG(ms, '1970-01-01', GETUTCDATE()))";
  }

  @Override
  public boolean supports(String driver) {
    return "com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(driver);
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
