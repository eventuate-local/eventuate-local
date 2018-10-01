package io.eventuate.local.common;

public class JdbcUrl {
  String host;
  int port;
  String database;
  String dbms;

  public JdbcUrl(String host, int port, String database, String dbms) {
    this.host = host;
    this.port = port;
    this.database = database;
    this.dbms = dbms;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getDatabase() {
    return database;
  }

  public String getDbms() {
    return dbms;
  }

  public boolean isMySql() {
    return "mysql".equalsIgnoreCase(dbms);
  }

  public boolean isPostgres() {
    return "postgresql".equalsIgnoreCase(dbms);
  }
}
