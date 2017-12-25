package io.eventuate.local.common;

import org.springframework.beans.factory.annotation.Value;

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

  private int postresWalIntervalInMilliseconds = 10;

  private int postgresReplicationStatusIntervalInMilliseconds = 1000;

  private String postgresReplicationSlotName = "eventuate_slot";

  public String getDbUserName() {
    return dbUserName;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public String getDbHistoryTopicName() {
    return dbHistoryTopicName;
  }

  public long getBinlogClientId() {
    return binlogClientId;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }

  public int getPollingIntervalInMilliseconds() {
    return pollingIntervalInMilliseconds;
  }

  public int getMaxEventsPerPolling() {
    return maxEventsPerPolling;
  }

  public int getMaxAttemptsForPolling() {
    return maxAttemptsForPolling;
  }

  public int getPollingRetryIntervalInMilliseconds() {
    return pollingRetryIntervalInMilliseconds;
  }

  public String getLeadershipLockPath() {
    return leadershipLockPath;
  }

  public String getOldDbHistoryTopicName() {
    return oldDbHistoryTopicName;
  }

  public String getMySqlBinLogClientName() {
    return mySqlBinLogClientName;
  }

  public int getBinlogConnectionTimeoutInMilliseconds() {
    return binlogConnectionTimeoutInMilliseconds;
  }

  public int getMaxAttemptsForBinlogConnection() {
    return maxAttemptsForBinlogConnection;
  }

  public void setMaxAttemptsForBinlogConnection(int maxAttemptsForBinlogConnection) {
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
  }

  public int getPostresWalIntervalInMilliseconds() {
    return postresWalIntervalInMilliseconds;
  }

  public void setPostresWalIntervalInMilliseconds(int postresWalIntervalInMilliseconds) {
    this.postresWalIntervalInMilliseconds = postresWalIntervalInMilliseconds;
  }

  public int getPostgresReplicationStatusIntervalInMilliseconds() {
    return postgresReplicationStatusIntervalInMilliseconds;
  }

  public void setPostgresReplicationStatusIntervalInMilliseconds(int postgresReplicationStatusIntervalInMilliseconds) {
    this.postgresReplicationStatusIntervalInMilliseconds = postgresReplicationStatusIntervalInMilliseconds;
  }

  public String getPostgresReplicationSlotName() {
    return postgresReplicationSlotName;
  }

  public void setPostgresReplicationSlotName(String postgresReplicationSlotName) {
    this.postgresReplicationSlotName = postgresReplicationSlotName;
  }
}
