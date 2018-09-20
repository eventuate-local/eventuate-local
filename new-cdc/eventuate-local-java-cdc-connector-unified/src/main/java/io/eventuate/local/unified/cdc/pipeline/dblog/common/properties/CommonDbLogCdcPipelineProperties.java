package io.eventuate.local.unified.cdc.pipeline.dblog.common.properties;

import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;

public abstract class CommonDbLogCdcPipelineProperties extends CdcPipelineProperties {
  private String dbHistoryTopicName = "db.history.topic";

  public String getDbHistoryTopicName() {
    return dbHistoryTopicName;
  }

  public void setDbHistoryTopicName(String dbHistoryTopicName) {
    this.dbHistoryTopicName = dbHistoryTopicName;
  }
}
