package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties;


import io.eventuate.local.unified.cdc.pipeline.dblog.common.properties.CommonDbLogCdcPipelineReaderProperties;

public class PostgresWalCdcPipelineReaderProperties extends CommonDbLogCdcPipelineReaderProperties {
  private Integer postgresWalIntervalInMilliseconds = 500;
  private Integer postgresReplicationStatusIntervalInMilliseconds = 1000;
  private String postgresReplicationSlotName = "eventuate_slot";

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
}
