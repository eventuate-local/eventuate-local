package io.eventuate.javaclient.spring.jdbc;

import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.jdbc.common.tests.CommonEventuateJdbcAccessImplTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class EventuateJdbcAccessImplTest extends CommonEventuateJdbcAccessImplTest {

  @Autowired
  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;

  @Autowired
  private EventuateJdbcAccess eventuateJdbcAccess;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Override
  protected EventuateSchema getEventuateSchema() {
    return eventuateSchema;
  }

  @Test
  @Override
  public void testSave() {
    super.testSave();
  }

  @Test
  @Override
  public void testFind() {
    super.testFind();
  }

  @Test
  @Override
  public void testUpdate() {
    super.testUpdate();
  }

  @Override
  protected EventuateJdbcStatementExecutor getEventuateJdbcStatementExecutor() {
    return eventuateJdbcStatementExecutor;
  }

  @Override
  protected EventuateJdbcAccess getEventuateJdbcAccess() {
    return eventuateJdbcAccess;
  }
}
