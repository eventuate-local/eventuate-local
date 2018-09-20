package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties;

import io.eventuate.local.unified.cdc.pipeline.dblog.common.properties.CommonDbLogCdcPipelineReaderProperties;
import org.springframework.util.Assert;

public class MySqlBinlogCdcPipelineReaderProperties extends CommonDbLogCdcPipelineReaderProperties {
  private String cdcDbUserName;
  private String cdcDbPassword;
  private Long binlogClientId = System.currentTimeMillis();

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

  public Long getBinlogClientId() {
    return binlogClientId;
  }

  public void setBinlogClientId(Long binlogClientId) {
    this.binlogClientId = binlogClientId;
  }
}
