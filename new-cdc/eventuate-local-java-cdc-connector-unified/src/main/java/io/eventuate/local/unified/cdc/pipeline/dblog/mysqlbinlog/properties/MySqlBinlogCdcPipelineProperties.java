package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties;

import io.eventuate.local.unified.cdc.pipeline.dblog.common.CommonDbLogCdcPipelineProperties;
import org.springframework.util.Assert;

public class MySqlBinlogCdcPipelineProperties extends CommonDbLogCdcPipelineProperties {
  private String cdcDbUserName;
  private String cdcDbPassword;
  private String sourceTableName = null;
  private Long binlogClientId = System.currentTimeMillis();
  private String oldDbHistoryTopicName = "eventuate.local.cdc.my-sql-connector.offset.storage";

  public void validate() {
    super.validate();
    Assert.notNull(cdcDbUserName, "cdcDbUserName must not be null");
    Assert.notNull(cdcDbPassword, "cdcDbPassword must not be null");
  }

  public String getCdcDbUserName() {
    return cdcDbUserName;
  }

  public void setCdcDbUserName(String cdcDbUserName) {
    this.cdcDbUserName = cdcDbUserName;
  }

  public String getCdcDbPassword() {
    return cdcDbPassword;
  }

  public void setCdcDbPassword(String cdcDbPassword) {
    this.cdcDbPassword = cdcDbPassword;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }

  public void setSourceTableName(String sourceTableName) {
    this.sourceTableName = sourceTableName;
  }

  public Long getBinlogClientId() {
    return binlogClientId;
  }

  public void setBinlogClientId(Long binlogClientId) {
    this.binlogClientId = binlogClientId;
  }

  public String getOldDbHistoryTopicName() {
    return oldDbHistoryTopicName;
  }

  public void setOldDbHistoryTopicName(String oldDbHistoryTopicName) {
    this.oldDbHistoryTopicName = oldDbHistoryTopicName;
  }
}
