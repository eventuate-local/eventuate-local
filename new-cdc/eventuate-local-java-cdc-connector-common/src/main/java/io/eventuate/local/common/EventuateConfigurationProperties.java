package io.eventuate.local.common;

import org.springframework.beans.factory.annotation.Value;

public class EventuateConfigurationProperties {

  @Value("${eventuatelocal.cdc.db.user.name:#{null}}")
  private String dbUserName;

  @Value("${eventuatelocal.cdc.db.password:#{null}}")
  private String dbPassword;

  @Value("${eventuatelocal.cdc.offset.storage.topic.name:#{\"offset.storage.topic\"}}")
  private String offsetStorageTopicName;

  @Value("${eventuatelocal.cdc.binlog.client.id:#{null}}")
  private Long binlogClientId;

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

  @Value("${eventuatelocal.cdc.read.old.debezium.db.offset.storage.topic:#{null}}")
  private Boolean readOldDebeziumDbOffsetStorageTopic;

  @Value("${eventuatelocal.cdc.mysql.binlog.client.name:#{null}}")
  private String mySqlBinlogClientName;

  @Value("${eventuatelocal.cdc.binlog.connection.timeout.in.milliseconds:#{5000}}")
  private int binlogConnectionTimeoutInMilliseconds;

  @Value("${eventuatelocal.cdc.max.attempts.for.binlog.connection:#{100}}")
  private int maxAttemptsForBinlogConnection;

  @Value("${eventuatelocal.cdc.replication.lag.measuring.interval.in.milliseconds:#{10000}}")
  private Long replicationLagMeasuringIntervalInMilliseconds;

  @Value("${eventuatelocal.cdc.monitoring.retry.interval.in.milliseconds:#{500}}")
  private int monitoringRetryIntervalInMilliseconds;

  @Value("${eventuatelocal.cdc.monitoring.retry.attempts:#{1000}}")
  private int monitoringRetryAttempts;

  private int postgresWalIntervalInMilliseconds = 500;

  private int postgresReplicationStatusIntervalInMilliseconds = 1000;

  private String postgresReplicationSlotName = "eventuate_slot";

  @Value("${eventuatelocal.cdc.additional.service.replication.slot.name:#{\"eventuate_offset_control_slot\"}}")
  private String additionalServiceReplicationSlotName;

  @Value("${eventuatelocal.cdc.wait.for.offset.sync.timeout.in.milliseconds:#{60000}}")
  private long waitForOffsetSyncTimeoutInMilliseconds;

  @Value("${eventuatelocal.cdc.use.gtid.when.possible:#{false}}")
  private boolean useGTIDsWhenPossible;

  public String getDbUserName() {
    return dbUserName;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public String getOffsetStorageTopicName() {
    return offsetStorageTopicName;
  }

  public Long getBinlogClientId() {
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

  public Boolean getReadOldDebeziumDbOffsetStorageTopic() {
    return readOldDebeziumDbOffsetStorageTopic;
  }

  public String getMySqlBinlogClientName() {
    return mySqlBinlogClientName;
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

  public int getPostgresWalIntervalInMilliseconds() {
    return postgresWalIntervalInMilliseconds;
  }

  public void setPostgresWalIntervalInMilliseconds(int postgresWalIntervalInMilliseconds) {
    this.postgresWalIntervalInMilliseconds = postgresWalIntervalInMilliseconds;
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

  public Long getReplicationLagMeasuringIntervalInMilliseconds() {
    return replicationLagMeasuringIntervalInMilliseconds;
  }

  public int getMonitoringRetryIntervalInMilliseconds() {
    return monitoringRetryIntervalInMilliseconds;
  }

  public int getMonitoringRetryAttempts() {
    return monitoringRetryAttempts;
  }

  public String getAdditionalServiceReplicationSlotName() {
    return additionalServiceReplicationSlotName;
  }

  public long getWaitForOffsetSyncTimeoutInMilliseconds() {
    return waitForOffsetSyncTimeoutInMilliseconds;
  }

  public boolean isUseGTIDsWhenPossible() {
    return useGTIDsWhenPossible;
  }
}
