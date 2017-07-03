package io.eventuate.local.mysql.binlog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdbcUrlParser {
  public static JdbcUrl parse(String dataSourceURL) {
    Pattern p = Pattern.compile("jdbc:mysql://([^:/]+)(:[0-9]+)?/([^?]+)(\\?.*)?$");
    Matcher m = p.matcher(dataSourceURL);

    if (!m.matches())
      throw new RuntimeException(dataSourceURL);

    String host = m.group(1);
    String port = m.group(2);
    String database = m.group(3);
    return new JdbcUrl(host, port == null ? 3306 : Integer.parseInt(port.substring(1)), database);
  }
}
