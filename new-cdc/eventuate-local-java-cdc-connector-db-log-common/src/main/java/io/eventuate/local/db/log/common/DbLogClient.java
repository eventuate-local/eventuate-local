package io.eventuate.local.db.log.common;

import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
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
  protected DbLogMetrics dbLogMetrics;
  private boolean checkEntriesForDuplicates;
  protected volatile boolean connected;

  public DbLogClient(DataProducerFactory dataProducerFactory,
                     CdcDataPublisherFactory cdcDataPublisherFactory,
                     MeterRegistry meterRegistry,
                     String dbUserName,
                     String dbPassword,
                     String dataSourceUrl,
                     CuratorFramework curatorFramework,
                     String leadershipLockPath,
                     DataSource dataSource,
                     long binlogClientUniqueId,
                     long replicationLagMeasuringIntervalInMilliseconds,
                     int monitoringRetryIntervalInMilliseconds,
                     int monitoringRetryAttempts) {

    super(dataProducerFactory,
            cdcDataPublisherFactory,
            meterRegistry,
            curatorFramework,
            leadershipLockPath,
            dataSourceUrl,
            dataSource,
            binlogClientUniqueId,
            monitoringRetryIntervalInMilliseconds,
            monitoringRetryAttempts);

    dbLogMetrics = new DbLogMetrics(meterRegistry,
            cdcMonitoringDao,
            binlogClientUniqueId,
            replicationLagMeasuringIntervalInMilliseconds);

    this.dbUserName = dbUserName;
    this.dbPassword = dbPassword;
    this.dataSourceUrl = dataSourceUrl;

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceUrl);
    host = jdbcUrl.getHost();
    port = jdbcUrl.getPort();
    defaultDatabase = jdbcUrl.getDatabase();
  }

  public boolean isConnected() {
    return connected;
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
    super.leaderStart();
    dbLogMetrics.start();
  }

  @Override
  protected void stopMetrics() {
    super.stopMetrics();
    dbLogMetrics.stop();
  }

  protected void onConnected() {
    dbLogMetrics.onConnected();
    connected = true;
  }

  protected void onDisconnected() {
    dbLogMetrics.onDisconnected();
    connected = false;
  }
}
