package io.eventuate.javaclient.spring.jdbc;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DefaultEventuateJdbcAccessImplTest.Config.class, properties = "eventuate.database.schema=")
public class DefaultEventuateJdbcAccessImplTest extends EventuateJdbcAccessImplTest {

  @Configuration
  @Import(CommonEventuateJdbcAccessImplTestConfiguration.class)
  public static class Config {
  }

  @Before
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
