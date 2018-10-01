package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;

public class ResolvedEventuateSchema {

  private String eventuateDatabaseSchema;

  public ResolvedEventuateSchema(String eventuateDatabaseSchema) {
    this.eventuateDatabaseSchema = eventuateDatabaseSchema;
  }

  public String getEventuateDatabaseSchema() {
    return eventuateDatabaseSchema;
  }

  public static ResolvedEventuateSchema make(EventuateSchema eventuateSchema, JdbcUrl jdbcUrl) {
    if (!eventuateSchema.isEmpty()) {
      return new ResolvedEventuateSchema(eventuateSchema.getEventuateDatabaseSchema());
    }

    if (jdbcUrl.isMySql()) {
      return new ResolvedEventuateSchema(jdbcUrl.getDatabase());
    }

    if (jdbcUrl.isPostgres()) {
      return new ResolvedEventuateSchema("public");
    }

    throw new IllegalArgumentException("Unknown database"); // should not be here (JdbcUrlParser has this check)
  }
 }