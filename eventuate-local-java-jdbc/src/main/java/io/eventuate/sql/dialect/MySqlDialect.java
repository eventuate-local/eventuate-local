package io.eventuate.sql.dialect;

import org.springframework.core.Ordered;

public class MySqlDialect extends DefaultEventuateSqlDialect implements Ordered {
  @Override
  public boolean supports(String driver) {
    return "com.mysql.jdbc.Driver".equals(driver);
  }

  @Override
  public String getCurrentTimeInMillisecondsExpression() {
    return "ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)";
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
