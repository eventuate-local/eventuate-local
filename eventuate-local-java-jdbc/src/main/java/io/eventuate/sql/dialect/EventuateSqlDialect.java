package io.eventuate.sql.dialect;

public interface EventuateSqlDialect {
  boolean supports(String driver);
  String getCurrentTimeInMillisecondsExpression();
  String addLimitToSql(String sql, String limitExpression);
}
