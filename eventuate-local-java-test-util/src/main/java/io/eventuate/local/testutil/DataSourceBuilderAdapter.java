package io.eventuate.local.testutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

public class DataSourceBuilderAdapter {
  private static final String SPRING_1_NAME = "org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder";
  private static final String SPRING_2_NAME = "org.springframework.boot.jdbc.DataSourceBuilder";

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private Object dataSourceBuilder;

  public DataSourceBuilderAdapter()
  {
    Class dataSourceBuilderClass = null;

    try {
      dataSourceBuilderClass = Class.forName(SPRING_1_NAME);
    } catch (ClassNotFoundException e) {
      logger.debug(e.getMessage(), e);
    }

    try {
      dataSourceBuilderClass = Class.forName(SPRING_2_NAME);
    } catch (ClassNotFoundException e) {
      logger.debug(e.getMessage(), e);
    }

    if (dataSourceBuilderClass == null) throw new RuntimeException("DataSourceBuilder class not found");

    try {
      dataSourceBuilder = dataSourceBuilderClass.getMethod("create").invoke(null);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public DataSourceBuilderAdapter url(String url) {
    invoke(() -> dataSourceBuilder.getClass().getMethod("url", String.class).invoke(dataSourceBuilder, url));
    return this;
  }

  public DataSourceBuilderAdapter driverClassName(String driverClassName) {
    invoke(() -> dataSourceBuilder.getClass().getMethod("driverClassName", String.class).invoke(dataSourceBuilder, driverClassName));
    return this;
  }

  public DataSourceBuilderAdapter username(String username) {
    invoke(() -> dataSourceBuilder.getClass().getMethod("username", String.class).invoke(dataSourceBuilder, username));
    return this;
  }

  public DataSourceBuilderAdapter password(String password) {
    invoke(() -> dataSourceBuilder.getClass().getMethod("password", String.class).invoke(dataSourceBuilder, password));
    return this;
  }

  public DataSource build() {
    return invoke(() -> (DataSource) dataSourceBuilder.getClass().getMethod("build").invoke(dataSourceBuilder));
  }

  private static <T> T invoke(Callable<T> invocation) {
    try {
      return invocation.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
