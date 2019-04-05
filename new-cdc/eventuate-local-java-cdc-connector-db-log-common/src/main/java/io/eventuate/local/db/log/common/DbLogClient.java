package io.eventuate.local.db.log.common;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.micrometer.core.instrument.MeterRegistry;

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
  protected CdcMonitoringDao cdcMonitoringDao;

  public DbLogClient(MeterRegistry meterRegistry,
                     String dbUserName,
                     String dbPassword,
                     String dataSourceUrl,
                     String leaderLockId,
                     LeaderSelectorFactory leaderSelectorFactory,
                     DataSource dataSource,
                     String readerName,
                     long replicationLagMeasuringIntervalInMilliseconds,
                     int monitoringRetryIntervalInMilliseconds,
                     int monitoringRetryAttempts) {

    super(meterRegistry,
            leaderLockId,
            leaderSelectorFactory,
            dataSourceUrl,
            dataSource,
            readerName);

    cdcMonitoringDao = new CdcMonitoringDao(dataSource,
            new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
            monitoringRetryIntervalInMilliseconds,
            monitoringRetryAttempts);

    dbLogMetrics = new DbLogMetrics(meterRegistry,
            cdcMonitoringDao,
            readerName,
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
