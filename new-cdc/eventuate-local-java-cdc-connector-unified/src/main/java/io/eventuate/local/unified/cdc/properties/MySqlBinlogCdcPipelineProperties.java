package io.eventuate.local.unified.cdc.properties;


import java.util.Optional;

public class MySqlBinlogCdcPipelineProperties extends CommonDbLogCdcPipelineProperties {
  private String sourceTableName; //null is default
  private Long binlogClientId;
  private String oldDbHistoryTopicName;
  private Integer binlogConnectionTimeoutInMilliseconds;
  private Integer maxAttemptsForBinlogConnection;

  public String getSourceTableName() {
    return sourceTableName;
  }

  public void setSourceTableName(String sourceTableName) {
    this.sourceTableName = sourceTableName;
  }

  public long getBinlogClientId() {
    return Optional.ofNullable(binlogClientId).orElse(System.currentTimeMillis());
  }

  public void setBinlogClientId(long binlogClientId) {
    this.binlogClientId = binlogClientId;
  }

  public String getOldDbHistoryTopicName() {
    return Optional.ofNullable(oldDbHistoryTopicName).orElse("eventuate.local.cdc.my-sql-connector.offset.storage");
  }

  public void setOldDbHistoryTopicName(String oldDbHistoryTopicName) {
    this.oldDbHistoryTopicName = oldDbHistoryTopicName;
  }

  public int getBinlogConnectionTimeoutInMilliseconds() {
    return Optional.ofNullable(binlogConnectionTimeoutInMilliseconds).orElse(5000);
  }

  public void setBinlogConnectionTimeoutInMilliseconds(int binlogConnectionTimeoutInMilliseconds) {
    this.binlogConnectionTimeoutInMilliseconds = binlogConnectionTimeoutInMilliseconds;
  }

  public int getMaxAttemptsForBinlogConnection() {
    return Optional.ofNullable(maxAttemptsForBinlogConnection).orElse(100);
  }

  public void setMaxAttemptsForBinlogConnection(int maxAttemptsForBinlogConnection) {
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
  }
}
