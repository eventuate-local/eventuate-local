package io.eventuate.javaclient.spring.jdbc;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.spring.id.ApplicationIdGeneratorCondition;
import io.eventuate.javaclient.jdbc.common.tests.EmbeddedSchemaModifier;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = EmptyEventuateJdbcAccessImplTest.Config.class, properties = "eventuate.database.schema=none")
public class EmptyEventuateJdbcAccessImplTest extends EventuateJdbcAccessImplTest {

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

  @BeforeEach
  public void init() {
    executeSql(embeddedSchemaModifier.getModifiedSqlLines(this::loadSqlScriptAsListOfLines));
    clear();
  }
}
