package io.eventuate.local.common;

import com.google.common.base.Objects;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import org.apache.commons.lang.builder.EqualsBuilder;

public class SchemaAndTable {
  private String schema;
  private String tableName;

  public SchemaAndTable(EventuateSchema schema, String tableName) {
    this.schema = schema.getEventuateDatabaseSchema();
    this.tableName = tableName;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schema, tableName);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}
