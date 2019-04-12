package io.eventuate.sql.dialect;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractDialectTest {
  @Autowired
  private SqlDialectSelector sqlDialectSelector;

  @Value("${spring.datasource.driver-class-name}")
  private String driver;

  private Class<? extends EventuateSqlDialect> expectedDialectClass;
  private String expectedCurrentTimeInMillisecondsExpression;


  public AbstractDialectTest(Class<? extends EventuateSqlDialect> expectedDialectClass,
                             String expectedCurrentTimeInMillisecondsExpression) {
    this.expectedDialectClass = expectedDialectClass;
    this.expectedCurrentTimeInMillisecondsExpression = expectedCurrentTimeInMillisecondsExpression;
  }

  @Test
  public void testDialect() {
    Assert.assertEquals(expectedDialectClass, sqlDialectSelector.getDialect(driver).getClass());

    Assert.assertEquals(expectedCurrentTimeInMillisecondsExpression,
            sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression());
  }
}
