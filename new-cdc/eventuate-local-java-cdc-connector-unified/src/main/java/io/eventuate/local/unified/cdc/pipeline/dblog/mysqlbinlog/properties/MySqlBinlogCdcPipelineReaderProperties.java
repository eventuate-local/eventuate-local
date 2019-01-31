package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties;

import io.eventuate.local.unified.cdc.pipeline.dblog.common.properties.CommonDbLogCdcPipelineReaderProperties;
import org.springframework.util.Assert;

public class MySqlBinlogCdcPipelineReaderProperties extends CommonDbLogCdcPipelineReaderProperties {
  private String cdcDbUserName;
  private String cdcDbPassword;
  private Boolean readOldDebeziumDbOffsetStorageTopic;
  private String mySqlBinlogClientName;

  public void validate() {
    super.validate();
    Assert.notNull(cdcDbUserName, "cdcDbUserName must not be null");
    Assert.notNull(cdcDbPassword, "cdcDbPassword must not be null");
    Assert.notNull(mySqlBinlogClientName, "mySqlBinlogClientName must not be null");
    Assert.notNull(readOldDebeziumDbOffsetStorageTopic,
            "readOldDebeziumDbOffsetStorageTopic must not be null");
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

  public Boolean getReadOldDebeziumDbOffsetStorageTopic() {
    return readOldDebeziumDbOffsetStorageTopic;
  }

  public void setReadOldDebeziumDbOffsetStorageTopic(Boolean readOldDebeziumDbOffsetStorageTopic) {
    this.readOldDebeziumDbOffsetStorageTopic = readOldDebeziumDbOffsetStorageTopic;
  }

  public String getMySqlBinlogClientName() {
    return mySqlBinlogClientName;
  }

  public void setMySqlBinlogClientName(String mySqlBinlogClientName) {
    this.mySqlBinlogClientName = mySqlBinlogClientName;
  }
}
