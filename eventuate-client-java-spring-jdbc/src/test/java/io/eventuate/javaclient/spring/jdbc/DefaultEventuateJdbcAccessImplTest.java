package io.eventuate.javaclient.spring.jdbc;

import io.eventuate.common.jdbc.EventuateSchema;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DefaultEventuateJdbcAccessImplTest.Config.class, EventuateJdbcAccessImplTest.Config.class})
@IntegrationTest
public class DefaultEventuateJdbcAccessImplTest extends EventuateJdbcAccessImplTest {

  public static class Config {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DataSource dataSource() {
      EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
      return builder.setType(EmbeddedDatabaseType.H2).addScript("eventuate-embedded-schema.sql").build();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventuateSchema eventuateSchema() {
      return new EventuateSchema();
    }
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
