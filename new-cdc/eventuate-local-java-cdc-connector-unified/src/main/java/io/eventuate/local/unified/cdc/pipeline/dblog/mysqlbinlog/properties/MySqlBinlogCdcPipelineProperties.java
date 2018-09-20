package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties;

import io.eventuate.local.unified.cdc.pipeline.dblog.common.properties.CommonDbLogCdcPipelineProperties;
import org.springframework.util.Assert;

public class MySqlBinlogCdcPipelineProperties extends CommonDbLogCdcPipelineProperties {
  private String oldDbHistoryTopicName = "eventuate.local.cdc.my-sql-connector.offset.storage";

  public void validate() {
    super.validate();
  }

  public String getOldDbHistoryTopicName() {
    return oldDbHistoryTopicName;
  }

  public void setOldDbHistoryTopicName(String oldDbHistoryTopicName) {
    this.oldDbHistoryTopicName = oldDbHistoryTopicName;
  }
}
