package io.eventuate.javaclient.spring.jdbc;

import io.eventuate.common.jdbc.EventuateSchema;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {CustomEventuateJdbcAccessImplTest.Config.class, EventuateJdbcAccessImplTest.Config.class})
@IntegrationTest
public class CustomEventuateJdbcAccessImplTest extends EventuateJdbcAccessImplTest {

  public static class Config {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DataSource dataSource() {
      EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
      return builder.setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventuateSchema eventuateSchema() {
      return new EventuateSchema("custom");
    }
  }

  @Override
  protected String readAllEventsSql() {
    return "select * from custom.events";
  }

  @Override
  protected String readAllEntitiesSql() {
    return "select * from custom.entities";
  }

  @Override
  protected String readAllSnapshots() {
    return "select * from custom.snapshots";
  }

  @Before
  public void init() throws Exception {
    List<String> lines = loadSqlScriptAsListOfLines("eventuate-embedded-schema.sql");
    for (int i = 0; i < 2; i++) lines.set(i, lines.get(i).replace("eventuate", "custom"));
    executeSql(lines);
  }
}
