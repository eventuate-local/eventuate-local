package io.eventuate.local.db.log.common;

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
  protected DbLogMetrics dbLogMetrics;
  private boolean checkEntriesForDuplicates;

  public DbLogClient(MeterRegistry meterRegistry,
                     HealthCheck healthCheck,
                     String dbUserName,
                     String dbPassword,
                     String dataSourceUrl,
                     CuratorFramework curatorFramework,
                     String leadershipLockPath,
                     DataSource dataSource,
                     long binlogClientUniqueId,
                     long replicationLagMeasuringIntervalInMilliseconds,
                     int monitoringRetryIntervalInMilliseconds,
                     int monitoringRetryAttempts,
                     int maxEventIntervalToAssumeReaderHealthy) {

    super(meterRegistry,
            healthCheck,
            curatorFramework,
            leadershipLockPath,
            dataSourceUrl,
            dataSource,
            binlogClientUniqueId,
            monitoringRetryIntervalInMilliseconds,
            monitoringRetryAttempts,
            maxEventIntervalToAssumeReaderHealthy);

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
  protected void leaderStop() {
    super.leaderStop();
    dbLogMetrics.stop();
  }

  protected void onConnected() {
    dbLogMetrics.onConnected();
    if (checkIfEventIsReceivedRecently()) {
      healthComponent.ifPresent(HealthCheck.HealthComponent::markAsHealthy);
    }
  }

  protected void onDisconnected() {
    dbLogMetrics.onDisconnected();
    healthComponent.ifPresent(hc -> hc.markAsUnhealthy(String.format("Reader with id %s disconnected", binlogClientUniqueId)));
  }
}
