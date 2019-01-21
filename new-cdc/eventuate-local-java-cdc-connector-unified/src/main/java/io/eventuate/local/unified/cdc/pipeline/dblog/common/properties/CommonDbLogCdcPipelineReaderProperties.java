package io.eventuate.local.unified.cdc.pipeline.dblog.common.properties;

import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;

public abstract class CommonDbLogCdcPipelineReaderProperties extends CdcPipelineReaderProperties {
  private String offsetStorageTopicName = "offset.storage.topic";
  private Integer binlogConnectionTimeoutInMilliseconds = 5000;
  private Integer maxAttemptsForBinlogConnection = 100;
  private Long replicationLagMeasuringIntervalInMilliseconds = 10000L;

  public String getOffsetStorageTopicName() {
    return offsetStorageTopicName;
  }

  public void setOffsetStorageTopicName(String offsetStorageTopicName) {
    this.offsetStorageTopicName = offsetStorageTopicName;
  }

  public Integer getBinlogConnectionTimeoutInMilliseconds() {
    return binlogConnectionTimeoutInMilliseconds;
  }

  public void setBinlogConnectionTimeoutInMilliseconds(Integer binlogConnectionTimeoutInMilliseconds) {
    this.binlogConnectionTimeoutInMilliseconds = binlogConnectionTimeoutInMilliseconds;
  }

  public Integer getMaxAttemptsForBinlogConnection() {
    return maxAttemptsForBinlogConnection;
  }

  public void setMaxAttemptsForBinlogConnection(Integer maxAttemptsForBinlogConnection) {
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
  }

  public Long getReplicationLagMeasuringIntervalInMilliseconds() {
    return replicationLagMeasuringIntervalInMilliseconds;
  }

  public void setReplicationLagMeasuringIntervalInMilliseconds(Long replicationLagMeasuringIntervalInMilliseconds) {
    this.replicationLagMeasuringIntervalInMilliseconds = replicationLagMeasuringIntervalInMilliseconds;
  }
}
