package io.eventuate.local.test.util;


public class SourceTableNameSupplier {
  private final String sourceTableName;

  public SourceTableNameSupplier(String sourceTableName) {

    this.sourceTableName = sourceTableName;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }
}
