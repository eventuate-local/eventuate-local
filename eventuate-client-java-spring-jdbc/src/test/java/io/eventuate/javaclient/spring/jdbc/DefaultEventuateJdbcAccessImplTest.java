package io.eventuate.javaclient.spring.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = DefaultEventuateJdbcAccessImplTest.Config.class, properties = "eventuate.database.schema=")
public class DefaultEventuateJdbcAccessImplTest extends EventuateJdbcAccessImplTest {

  @Configuration
  @Import(CommonEventuateJdbcAccessImplTestConfiguration.class)
  public static class Config {
  }

  @BeforeEach
  public void init() {
    clear();
  }

  @Override
  protected String readAllEventsSql() {
    return "select * from eventuate.events";
  }

  @Override
  protected String readAllEntitiesSql() {
    return "select * from eventuate.entities";
  }

  @Override
  protected String readAllSnapshots() {
    return "select * from eventuate.snapshots";
  }
}
