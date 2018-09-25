package io.eventuate.local.db.log.common;

import io.eventuate.local.common.*;
import org.apache.curator.framework.CuratorFramework;

public abstract class DbLogClient extends BinlogEntryReader {

  protected String dbUserName;
  protected String dbPassword;
  protected String dataSourceUrl;
  protected String host;
  protected int port;
  protected String defaultDatabase;


  public DbLogClient(String dbUserName,
                     String dbPassword,
                     String dataSourceUrl,
                     CuratorFramework curatorFramework,
                     String leadershipLockPath) {

    super(curatorFramework, leadershipLockPath);

    this.dbUserName = dbUserName;
    this.dbPassword = dbPassword;
    this.dataSourceUrl = dataSourceUrl;

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceUrl);
    host = jdbcUrl.getHost();
    port = jdbcUrl.getPort();
    defaultDatabase = jdbcUrl.getDatabase();
  }
}
