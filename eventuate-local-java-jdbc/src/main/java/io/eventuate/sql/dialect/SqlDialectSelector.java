package io.eventuate.sql.dialect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;

import java.util.Collection;

public class SqlDialectSelector {
  @Autowired
  private Collection<EventuateSqlDialect> sqlDialects;

  EventuateSqlDialect selectDialect(Collection<EventuateSqlDialect> sqlDialects, String driver) {
    return sqlDialects
            .stream()
            .filter(dialect -> dialect.supports(driver))
            .min(OrderComparator.INSTANCE)
            .orElseThrow(() ->
                    new IllegalStateException(String.format("Sql Dialect not found (%s), " +
                                    "you can specify environment variable '%s' to solve the issue",
                            driver,
                            "EVENTUATE_CURRENT_TIME_IN_MILLISECONDS_SQL")));
  }

  public EventuateSqlDialect getDialect(String driver) {
    return selectDialect(sqlDialects, driver);
  }
}
