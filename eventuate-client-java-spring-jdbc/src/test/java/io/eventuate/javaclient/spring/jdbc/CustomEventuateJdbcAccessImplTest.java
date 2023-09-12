package io.eventuate.javaclient.spring.jdbc;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.spring.id.ApplicationIdGeneratorCondition;
import io.eventuate.javaclient.jdbc.common.tests.EmbeddedSchemaModifier;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CustomEventuateJdbcAccessImplTest.Config.class, properties = "eventuate.database.schema=custom")
public class CustomEventuateJdbcAccessImplTest extends EventuateJdbcAccessImplTest {

  @Configuration
  @Import(CommonEventuateJdbcAccessImplTestConfiguration.class)
  public static class Config {

    @Bean
    @Conditional(ApplicationIdGeneratorCondition.class)
    public EmbeddedSchemaModifier embeddedSchemaModifier(EventuateSchema eventuateSchema) {
      return new EmbeddedSchemaModifier(eventuateSchema,"eventuate-embedded-schema.sql");
    }

    @Bean
    @ConditionalOnProperty(name = "eventuate.outbox.id")
    public EmbeddedSchemaModifier embeddedSchemaModifierDbId(EventuateSchema eventuateSchema) {
      return new EmbeddedSchemaModifier(eventuateSchema,"eventuate-embedded-schema-db-id.sql");
    }
  }

  @Autowired
  private EmbeddedSchemaModifier embeddedSchemaModifier;

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
  public void init() {
    executeSql(embeddedSchemaModifier.getModifiedSqlLines(this::loadSqlScriptAsListOfLines));
    clear();
  }
}
