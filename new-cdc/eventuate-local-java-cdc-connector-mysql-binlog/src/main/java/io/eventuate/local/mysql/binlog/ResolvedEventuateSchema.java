package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.JdbcUrl;

public class ResolvedEventuateSchema {

  private String eventuateDatabaseSchema;

  public ResolvedEventuateSchema(String eventuateDatabaseSchema) {
    this.eventuateDatabaseSchema = eventuateDatabaseSchema;
  }

  public String getEventuateDatabaseSchema() {
    return eventuateDatabaseSchema;
  }


  public static ResolvedEventuateSchema make(EventuateSchema eventuateSchema, JdbcUrl jdbcUrl) {
    return new ResolvedEventuateSchema(eventuateSchema.isEmpty() ? jdbcUrl.getDatabase() : eventuateSchema.getEventuateDatabaseSchema());
  }

}
