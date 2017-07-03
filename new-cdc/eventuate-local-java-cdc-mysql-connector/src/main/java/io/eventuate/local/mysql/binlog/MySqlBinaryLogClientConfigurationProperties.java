package io.eventuate.local.mysql.binlog;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("eventuateLocal.cdc")
public class MySqlBinaryLogClientConfigurationProperties {

  //  @NotBlank
  private String dbUserName;

  //  @NotBlank
  private String dbPassword;

  private String dbHistoryTopicName = "db.history.topic";

  private long binlogClientId = System.currentTimeMillis();

  private String sourceTableName;

  public String getDbUserName() {
    return dbUserName;
  }

  public void setDbUserName(String dbUserName) {
    this.dbUserName = dbUserName;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public void setDbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }

  public String getDbHistoryTopicName() {
    return dbHistoryTopicName;
  }

  public void setDbHistoryTopicName(String dbHistoryTopicName) {
    this.dbHistoryTopicName = dbHistoryTopicName;
  }

  public long getBinlogClientId() {
    return binlogClientId;
  }

  public void setBinlogClientId(long binlogClientId) {
    this.binlogClientId = binlogClientId;
  }

  public void setSourceTableName(String sourceTableName) {
    this.sourceTableName = sourceTableName;
  }

  public String getSourceTableName() {

    return sourceTableName;
  }
}
