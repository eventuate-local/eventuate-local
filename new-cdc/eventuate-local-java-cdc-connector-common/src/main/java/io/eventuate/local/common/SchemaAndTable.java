package io.eventuate.local.common;

import com.google.common.base.Objects;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import org.apache.commons.lang.builder.EqualsBuilder;

public class SchemaAndTable {
  private String schema;
  private String tableName;

  public SchemaAndTable(String schema, String tableName) {
    this.schema = schema;
    this.tableName = tableName.toLowerCase();
  }

  public String getSchema() {
    return schema;
  }

  public String getTableName() {
    return tableName;
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
