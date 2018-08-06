package io.eventuate.local.unified.cdc.properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CdcPipelineProperties {
  private String dataSourceUrl;
  private String dbUserName;
  private String dbPassword;
  private String dbDriverClassName;

  private String type;
  private String leadershipLockPath;
  private String eventuateDatabaseSchema; //null is default //TODO: move schema in orgiginal configs to common configuraiton

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLeadershipLockPath() {
    return leadershipLockPath;
  }

  public void setLeadershipLockPath(String leadershipLockPath) {
    this.leadershipLockPath = leadershipLockPath;
  }

  public String getEventuateDatabaseSchema() {
    return eventuateDatabaseSchema;
  }

  public void setEventuateDatabaseSchema(String eventuateDatabaseSchema) {
    this.eventuateDatabaseSchema = eventuateDatabaseSchema;
  }


  public String getDataSourceUrl() {
    return dataSourceUrl;
  }

  public void setDataSourceUrl(String dataSourceUrl) {
    this.dataSourceUrl = dataSourceUrl;
  }

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

  public String getDbDriverClassName() {
    return dbDriverClassName;
  }

  public void setDbDriverClassName(String dbDriverClassName) {
    this.dbDriverClassName = dbDriverClassName;
  }
}
