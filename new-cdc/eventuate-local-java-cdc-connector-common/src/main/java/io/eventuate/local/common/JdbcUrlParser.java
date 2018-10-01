package io.eventuate.local.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdbcUrlParser {
  public static JdbcUrl parse(String dataSourceURL) {
    Pattern p = Pattern.compile("jdbc:([a-zA-Z0-9]+)://([^:/]+)(:[0-9]+)?/([^?]+)(\\?.*)?$");
    Matcher m = p.matcher(dataSourceURL);

    if (!m.matches())
      throw new RuntimeException(dataSourceURL);

    String dbms = m.group(1);
    String host = m.group(2);
    String port = m.group(3);
    String database = m.group(4);

    int parsedPort;

    if (port == null) {
      if ("mysql".equalsIgnoreCase(dbms)) {
        parsedPort = 3306;
      } else if ("postgresql".equalsIgnoreCase(dbms)) {
        parsedPort = 5432;
      } else {
        throw new IllegalArgumentException("Unknown database");
      }
    } else {
      parsedPort = Integer.parseInt(port.substring(1));
    }

    return new JdbcUrl(host, parsedPort, database, dbms);
  }
}
