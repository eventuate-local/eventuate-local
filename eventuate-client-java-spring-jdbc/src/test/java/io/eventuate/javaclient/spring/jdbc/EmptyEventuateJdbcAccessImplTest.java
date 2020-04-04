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
@SpringApplicationConfiguration(classes = {EmptyEventuateJdbcAccessImplTest.Config.class, EventuateJdbcAccessImplTest.Config.class})
@IntegrationTest
public class EmptyEventuateJdbcAccessImplTest extends EventuateJdbcAccessImplTest {

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
      return new EventuateSchema(EventuateSchema.EMPTY_SCHEMA);
    }
  }

  @Override
  protected String readAllEventsSql() {
    return "select * from events";
  }

  @Override
  protected String readAllEntitiesSql() {
    return "select * from entities";
  }

  @Override
  protected String readAllSnapshots() {
    return "select * from snapshots";
  }

  @Before
  public void init() throws Exception {
    List<String> lines = loadSqlScriptAsListOfLines("/eventuate-embedded-schema.sql");
    lines = lines.subList(2, lines.size());
    executeSql(lines);
  }
}
