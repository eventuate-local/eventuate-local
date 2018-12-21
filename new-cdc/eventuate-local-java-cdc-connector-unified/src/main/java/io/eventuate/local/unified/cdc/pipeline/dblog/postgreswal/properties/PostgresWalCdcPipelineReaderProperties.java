package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties;


import io.eventuate.local.unified.cdc.pipeline.dblog.common.properties.CommonDbLogCdcPipelineReaderProperties;

public class PostgresWalCdcPipelineReaderProperties extends CommonDbLogCdcPipelineReaderProperties {
  private Integer postgresWalIntervalInMilliseconds = 500;
  private Integer postgresReplicationStatusIntervalInMilliseconds = 1000;
  private String postgresReplicationSlotName = "eventuate_slot";
  private String additionalServiceReplicationSlotName = "eventuate_offset_control_slot";
  private long waitForOffsetSyncTimeoutInMilliseconds = 60000;

  public Integer getPostgresWalIntervalInMilliseconds() {
    return postgresWalIntervalInMilliseconds;
  }

  public void setPostgresWalIntervalInMilliseconds(Integer postgresWalIntervalInMilliseconds) {
    this.postgresWalIntervalInMilliseconds = postgresWalIntervalInMilliseconds;
  }

  public Integer getPostgresReplicationStatusIntervalInMilliseconds() {
    return postgresReplicationStatusIntervalInMilliseconds;
  }

  public void setPostgresReplicationStatusIntervalInMilliseconds(Integer postgresReplicationStatusIntervalInMilliseconds) {
    this.postgresReplicationStatusIntervalInMilliseconds = postgresReplicationStatusIntervalInMilliseconds;
  }

  public String getPostgresReplicationSlotName() {
    return postgresReplicationSlotName;
  }

  public void setPostgresReplicationSlotName(String postgresReplicationSlotName) {
    this.postgresReplicationSlotName = postgresReplicationSlotName;
  }

  public String getAdditionalServiceReplicationSlotName() {
    return additionalServiceReplicationSlotName;
  }

  public void setAdditionalServiceReplicationSlotName(String additionalServiceReplicationSlotName) {
    this.additionalServiceReplicationSlotName = additionalServiceReplicationSlotName;
  }

  public long getWaitForOffsetSyncTimeoutInMilliseconds() {
    return waitForOffsetSyncTimeoutInMilliseconds;
  }

  public void setWaitForOffsetSyncTimeoutInMilliseconds(long waitForOffsetSyncTimeoutInMilliseconds) {
    this.waitForOffsetSyncTimeoutInMilliseconds = waitForOffsetSyncTimeoutInMilliseconds;
  }
}
