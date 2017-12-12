package io.eventuate.local.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class EventuateConfigurationProperties {

  @Value("${eventuatelocal.cdc.db.user.name:#{null}}")
  private String dbUserName;

  @Value("${eventuatelocal.cdc.db.password:#{null}}")
  private String dbPassword;

  @Value("${eventuatelocal.cdc.db.history.topic.name:#{\"db.history.topic\"}}")
  private String dbHistoryTopicName;

  @Value("${eventuatelocal.cdc.binlog.client.id:#{T(java.lang.System).currentTimeMillis()}}")
  private long binlogClientId;

  @Value("${eventuatelocal.cdc.source.table.name:#{null}}")
  private String sourceTableName;

  @Value("${eventuatelocal.cdc.polling.interval.in.milliseconds:#{500}}")
  private int pollingIntervalInMilliseconds;

  @Value("${eventuatelocal.cdc.max.events.per.polling:#{1000}}")
  private int maxEventsPerPolling;

  @Value("${eventuatelocal.cdc.max.attempts.for.polling:#{100}}")
  private int maxAttemptsForPolling;

  @Value("${eventuatelocal.cdc.polling.retry.interval.in.milleseconds:#{500}}")
  private int pollingRetryIntervalInMilliseconds;

  @Value("${eventuatelocal.cdc.leadership.lock.path:#{\"/eventuatelocal/cdc/leader\"}}")
  private String leadershipLockPath;

  @Value("${eventuatelocal.cdc.old.db.history.topic.name:#{\"eventuate.local.cdc.my-sql-connector.offset.storage\"}}")
  private String oldDbHistoryTopicName;

  @Value("${eventuatelocal.cdc.my.sql.bin.log.client.name:#{\"MySqlBinLog\"}}")
  private String mySqlBinLogClientName;

  @Value("${eventuatelocal.cdc.binlog.connection.timeout.in.milliseconds:#{5000}}")
  private int binlogConnectionTimeoutInMilliseconds;

  @Value("${eventuatelocal.cdc.max.attempts.for.binlog.connection:#{100}}")
  private int maxAttemptsForBinlogConnection;

  public String getDbUserName() {
    return dbUserName;
  }

  public void setDbUserName(String dbUserName) {
    this.dbUserName = dbUserName;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public void setDbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }

  public String getDbHistoryTopicName() {
    return dbHistoryTopicName;
  }

  public void setDbHistoryTopicName(String dbHistoryTopicName) {
    this.dbHistoryTopicName = dbHistoryTopicName;
  }

  public long getBinlogClientId() {
    return binlogClientId;
  }

  public void setBinlogClientId(long binlogClientId) {
    this.binlogClientId = binlogClientId;
  }

  public void setSourceTableName(String sourceTableName) {
    this.sourceTableName = sourceTableName;
  }

  public String getSourceTableName() {

    return sourceTableName;
  }

  public int getPollingIntervalInMilliseconds() {
    return pollingIntervalInMilliseconds;
  }

  public void setPollingIntervalInMilliseconds(int pollingIntervalInMilliseconds) {
    this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
  }

  public int getMaxEventsPerPolling() {
    return maxEventsPerPolling;
  }

  public void setMaxEventsPerPolling(int maxEventsPerPolling) {
    this.maxEventsPerPolling = maxEventsPerPolling;
  }

  public int getMaxAttemptsForPolling() {
    return maxAttemptsForPolling;
  }

  public void setMaxAttemptsForPolling(int maxAttemptsForPolling) {
    this.maxAttemptsForPolling = maxAttemptsForPolling;
  }

  public int getPollingRetryIntervalInMilliseconds() {
    return pollingRetryIntervalInMilliseconds;
  }

  public void setPollingRetryIntervalInMilliseconds(int pollingRetryIntervalInMilliseconds) {
    this.pollingRetryIntervalInMilliseconds = pollingRetryIntervalInMilliseconds;
  }

  public String getLeadershipLockPath() {
    return leadershipLockPath;
  }

  public void setLeadershipLockPath(String leadershipLockPath) {
    this.leadershipLockPath = leadershipLockPath;
  }

  public String getOldDbHistoryTopicName() {
    return oldDbHistoryTopicName;
  }

  public void setOldDbHistoryTopicName(String oldDbHistoryTopicName) {
    this.oldDbHistoryTopicName = oldDbHistoryTopicName;
  }

  public String getMySqlBinLogClientName() {
    return mySqlBinLogClientName;
  }

  public void setMySqlBinLogClientName(String mySqlBinLogClientName) {
    this.mySqlBinLogClientName = mySqlBinLogClientName;
  }

  public int getBinlogConnectionTimeoutInMilliseconds() {
    return binlogConnectionTimeoutInMilliseconds;
  }

  public void setBinlogConnectionTimeoutInMilliseconds(int binlogConnectionTimeoutInMilliseconds) {
    this.binlogConnectionTimeoutInMilliseconds = binlogConnectionTimeoutInMilliseconds;
  }

  public int getMaxAttemptsForBinlogConnection() {
    return maxAttemptsForBinlogConnection;
  }

  public void setMaxAttemptsForBinlogConnection(int maxAttemptsForBinlogConnection) {
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
  }
}
