package io.eventuate.local.unified.cdc.pipeline.common.properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.springframework.util.Assert;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CdcPipelineReaderProperties implements ValidatableProperties {
  private String type;

  private String dataSourceUrl;
  private String dataSourceUserName;
  private String dataSourcePassword;
  private String dataSourceDriverClassName;
  private String leadershipLockPath;
  private String readerName;
  private int monitoringRetryIntervalInMilliseconds = 500;
  private int monitoringRetryAttempts = 1000;

  @Override
  public void validate() {
    Assert.notNull(type, "type must not be null");
    Assert.notNull(dataSourceUrl, "dataSourceUrl must not be null");
    Assert.notNull(dataSourceUserName, "dataSourceUserName must not be null");
    Assert.notNull(dataSourcePassword, "dataSourcePassword must not be null");
    Assert.notNull(dataSourceDriverClassName, "dataSourceDriverClassName must not be null");
    Assert.notNull(leadershipLockPath, "leadershipLockPath must not be null");
    Assert.notNull(readerName, "readerName must not be null");
  }

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

  public String getDataSourceUrl() {
    return dataSourceUrl;
  }

  public void setDataSourceUrl(String dataSourceUrl) {
    this.dataSourceUrl = dataSourceUrl;
  }

  public String getDataSourceUserName() {
    return dataSourceUserName;
  }

  public void setDataSourceUserName(String dataSourceUserName) {
    this.dataSourceUserName = dataSourceUserName;
  }

  public String getDataSourcePassword() {
    return dataSourcePassword;
  }

  public void setDataSourcePassword(String dataSourcePassword) {
    this.dataSourcePassword = dataSourcePassword;
  }

  public String getDataSourceDriverClassName() {
    return dataSourceDriverClassName;
  }

  public void setDataSourceDriverClassName(String dataSourceDriverClassName) {
    this.dataSourceDriverClassName = dataSourceDriverClassName;
  }

  public String getReaderName() {
    return readerName;
  }

  public void setReaderName(String readerName) {
    this.readerName = readerName;
  }


  public int getMonitoringRetryIntervalInMilliseconds() {
    return monitoringRetryIntervalInMilliseconds;
  }

  public void setMonitoringRetryIntervalInMilliseconds(int monitoringRetryIntervalInMilliseconds) {
    this.monitoringRetryIntervalInMilliseconds = monitoringRetryIntervalInMilliseconds;
  }

  public int getMonitoringRetryAttempts() {
    return monitoringRetryAttempts;
  }

  public void setMonitoringRetryAttempts(int monitoringRetryAttempts) {
    this.monitoringRetryAttempts = monitoringRetryAttempts;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }
}
