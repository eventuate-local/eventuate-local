package io.eventuate.local.unified.cdc.properties;

import java.util.Optional;

public class CommonDbLogCdcPipelineProperties extends CdcPipelineProperties {
  private String dbHistoryTopicName;
  private String mySqlBinLogClientName;

  public String getDbHistoryTopicName() {
    return Optional.ofNullable(dbHistoryTopicName).orElse("db.history.topic");
  }

  public void setDbHistoryTopicName(String dbHistoryTopicName) {
    this.dbHistoryTopicName = dbHistoryTopicName;
  }

  public String getMySqlBinLogClientName() {
    return Optional.ofNullable(mySqlBinLogClientName).orElse("MySqlBinLog");
  }

  public void setMySqlBinLogClientName(String mySqlBinLogClientName) {
    this.mySqlBinLogClientName = mySqlBinLogClientName;
  }
}
