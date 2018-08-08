package io.eventuate.local.unified.cdc.properties;


public class PostgresWalCdcPipelineProperties extends CommonDbLogCdcPipelineProperties {
  private Integer postgresWalIntervalInMilliseconds = 10;
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
