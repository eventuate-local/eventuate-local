package io.eventuate.local.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.cdc")
public class EventuateConfigurationProperties {

  //  @NotBlank
  private String dbUserName;

  //  @NotBlank
  private String dbPassword;

  private String dbHistoryTopicName = "db.history.topic";

  private long binlogClientId = System.currentTimeMillis();

  private String sourceTableName;

  private int pollingIntervalInMilliseconds = 500;

  private int maxEventsPerPolling = 1000;

  private int maxAttemptsForPolling = 100;

  private int pollingRetryIntervalInMilliseconds = 500;

  private String leadershipLockPath = "/eventuatelocal/cdc/leader";

  private String oldDbHistoryTopicName = "eventuate.local.cdc.my-sql-connector.offset.storage";

  private String mySqlBinLogClientName = "MySqlBinLog";

  private int binlogConnectionTimeoutInMilliseconds = 5000;

  private int maxAttemptsForBinlogConnection = 100;

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
