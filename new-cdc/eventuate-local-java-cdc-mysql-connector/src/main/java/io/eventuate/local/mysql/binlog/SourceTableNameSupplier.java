package io.eventuate.local.mysql.binlog;


public class SourceTableNameSupplier {
  private final String sourceTableName;
  private final String defaultSourceTableName;

  public SourceTableNameSupplier(String sourceTableName, String defaultSourceTableName) {
    this.sourceTableName = sourceTableName;
    this.defaultSourceTableName = defaultSourceTableName;
  }

  public String getSourceTableName() {
    return sourceTableName == null ? defaultSourceTableName : sourceTableName;
  }
}
