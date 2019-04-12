package io.eventuate.sql.dialect;

import io.eventuate.javaclient.spring.jdbc.DefaultEventuateSqlDialect;
import org.springframework.core.Ordered;

public class MySqlDialect extends DefaultEventuateSqlDialect implements Ordered {
  @Override
  public boolean supports(String driver) {
    return "com.mysql.jdbc.Driver".equals(driver);
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
