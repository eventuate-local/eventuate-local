package io.eventuate.local.db.log.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;
import java.util.Optional;

public abstract class DbLogClient extends BinlogEntryReader {

  protected String dbUserName;
  protected String dbPassword;
  protected String host;
  protected int port;
  protected String defaultDatabase;
  private boolean checkEntriesForDuplicates;
  private CdcMonitoringDataPublisher cdcMonitoringDataPublisher;

  public DbLogClient(MeterRegistry meterRegistry,
                     String dbUserName,
                     String dbPassword,
                     String dataSourceUrl,
                     CuratorFramework curatorFramework,
                     String leadershipLockPath,
                     DataSource dataSource,
                     long binlogClientUniqueId,
                     long replicationLagMeasuringIntervalInMilliseconds) {

    super(meterRegistry, curatorFramework, leadershipLockPath, dataSourceUrl, dataSource, binlogClientUniqueId);

    this.dbUserName = dbUserName;
    this.dbPassword = dbPassword;
    this.dataSourceUrl = dataSourceUrl;

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceUrl);
    host = jdbcUrl.getHost();
    port = jdbcUrl.getPort();
    defaultDatabase = jdbcUrl.getDatabase();

    CdcMonitoringDao cdcMonitoringDao = new CdcMonitoringDao(dataSource, new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA));

    cdcMonitoringDataPublisher = new CdcMonitoringDataPublisher(meterRegistry,
            cdcMonitoringDao,
            binlogClientUniqueId,
            replicationLagMeasuringIntervalInMilliseconds);
  }

  @Override
  public void start() {
    checkEntriesForDuplicates = true;
    super.start();
  }

  protected boolean shouldSkipEntry(Optional<BinlogFileOffset> startingBinlogFileOffset, BinlogFileOffset offset) {
    if (checkEntriesForDuplicates) {
      if (startingBinlogFileOffset.isPresent()) {
        BinlogFileOffset startingOffset = startingBinlogFileOffset.get();

        if (startingOffset.isSameOrAfter(offset)) {
          return true;
        }
      }

      checkEntriesForDuplicates = false;
    }

    return false;
  }

  @Override
  protected void leaderStart() {
    cdcMonitoringDataPublisher.start();
  }

  protected void leaderStop() {
    super.leaderStop();

    cdcMonitoringDataPublisher.stop();
  }

  protected void onMonitoringEventReceived(long timestamp) {
    cdcMonitoringDataPublisher.eventReceived(timestamp);
  }
}
