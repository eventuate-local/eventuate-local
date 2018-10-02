package io.eventuate.local.common;

public class JdbcUrl {

  String host;
  int port;
  String database;

  public JdbcUrl(String host, int port, String database) {
    this.host = host;
    this.port = port;
    this.database = database;
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
}
