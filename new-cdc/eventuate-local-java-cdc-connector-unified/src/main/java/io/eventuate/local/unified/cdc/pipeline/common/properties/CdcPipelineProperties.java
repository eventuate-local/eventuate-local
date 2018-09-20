package io.eventuate.local.unified.cdc.pipeline.common.properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.springframework.util.Assert;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CdcPipelineProperties implements ValidatableProperties {
  private String type;

  private String reader;
  private String eventuateDatabaseSchema = null;
  private String sourceTableName = null;

  public void validate() {
    Assert.notNull(type, "type must not be null");
    Assert.notNull(reader, "reader must not be null");
  }

  public String getReader() {
    return reader;
  }

  public void setReader(String reader) {
    this.reader = reader;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getEventuateDatabaseSchema() {
    return eventuateDatabaseSchema;
  }

  public void setEventuateDatabaseSchema(String eventuateDatabaseSchema) {
    this.eventuateDatabaseSchema = eventuateDatabaseSchema;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }

  public String getSourceTableName() {
    return sourceTableName;
  }

  public void setSourceTableName(String sourceTableName) {
    this.sourceTableName = sourceTableName;
  }
}
