package io.eventuate.local.db.log.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import org.apache.curator.framework.CuratorFramework;

import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class DbLogClient<HANDLER extends BinlogEntryHandler> extends BinlogEntryReader<HANDLER> {

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

  public abstract void addBinlogEntryHandler(EventuateSchema eventuateSchema,
                                             String sourceTableName,
                                             BiConsumer<BinlogEntry, Optional<BinlogFileOffset>> eventConsumer);
}
