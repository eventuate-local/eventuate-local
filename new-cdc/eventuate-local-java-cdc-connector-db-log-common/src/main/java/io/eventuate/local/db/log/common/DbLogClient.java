package io.eventuate.local.db.log.common;

import io.eventuate.local.common.*;
import org.apache.curator.framework.CuratorFramework;

import java.util.Optional;

public abstract class DbLogClient extends BinlogEntryReader {

  protected String dbUserName;
  protected String dbPassword;
  protected String host;
  protected int port;
  protected String defaultDatabase;
  protected OffsetStore offsetStore;
  private boolean checkEntriesForDuplicates;

  public DbLogClient(String dbUserName,
                     String dbPassword,
                     String dataSourceUrl,
                     CuratorFramework curatorFramework,
                     String leadershipLockPath,
                     OffsetStore offsetStore) {

    super(curatorFramework, leadershipLockPath, dataSourceUrl);

    this.dbUserName = dbUserName;
    this.dbPassword = dbPassword;
    this.dataSourceUrl = dataSourceUrl;
    this.offsetStore = offsetStore;

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceUrl);
    host = jdbcUrl.getHost();
    port = jdbcUrl.getPort();
    defaultDatabase = jdbcUrl.getDatabase();
  }

  @Override
  public void start() {
    checkEntriesForDuplicates = true;
    super.start();
  }

  protected boolean shouldSkipEntry(Optional<BinlogFileOffset> startingBinlogFileOffset, BinlogEntry entry) {
    if (checkEntriesForDuplicates) {
      if (startingBinlogFileOffset.isPresent()) {
        BinlogFileOffset startingOffset = startingBinlogFileOffset.get();

        if (startingOffset.isSameOrAfter(entry.getBinlogFileOffset())) {
          return true;
        }
      }

      checkEntriesForDuplicates = false;
    }

    return false;
  }
}
