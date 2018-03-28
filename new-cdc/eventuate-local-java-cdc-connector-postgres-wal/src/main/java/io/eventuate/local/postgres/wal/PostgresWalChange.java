package io.eventuate.local.postgres.wal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostgresWalChange {

  private String kind;
  private String schema;
  private String table;
  private String[] columnnames;
  private String[] columntypes;
  private String[] columnvalues;

  public PostgresWalChange() {
  }

  public PostgresWalChange(String kind, String schema, String table, String[] columnnames, String[] columntypes, String[] columnvalues) {
    this.kind = kind;
    this.schema = schema;
    this.table = table;
    this.columnnames = columnnames;
    this.columntypes = columntypes;
    this.columnvalues = columnvalues;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String[] getColumnnames() {
    return columnnames;
  }

  public void setColumnnames(String[] columnnames) {
    this.columnnames = columnnames;
  }

  public String[] getColumntypes() {
    return columntypes;
  }

  public void setColumntypes(String[] columntypes) {
    this.columntypes = columntypes;
  }

  public String[] getColumnvalues() {
    return columnvalues;
  }

  public void setColumnvalues(String[] columnvalues) {
    this.columnvalues = columnvalues;
  }
}