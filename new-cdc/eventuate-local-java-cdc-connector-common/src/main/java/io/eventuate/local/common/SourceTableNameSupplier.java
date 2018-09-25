package io.eventuate.local.common;


public class SourceTableNameSupplier {
  private final String sourceTableName;
  private final String defaultSourceTableName;
  private final String idField;
  private final String publishedField;

  public SourceTableNameSupplier(String sourceTableName,
                                 String defaultSourceTableName,
                                 String idField,
                                 String publishedField) {

    this.sourceTableName = sourceTableName;
    this.defaultSourceTableName = defaultSourceTableName;
    this.idField = idField;
    this.publishedField = publishedField;
  }

  public String getSourceTableName() {
    return sourceTableName == null ? defaultSourceTableName : sourceTableName;
  }

  public String getIdField() {
    return idField;
  }

  public String getPublishedField() {
    return publishedField;
  }
}
