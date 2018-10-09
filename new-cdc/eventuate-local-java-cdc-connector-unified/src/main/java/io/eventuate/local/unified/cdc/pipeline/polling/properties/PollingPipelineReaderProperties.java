package io.eventuate.local.unified.cdc.pipeline.polling.properties;


import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;

public class PollingPipelineReaderProperties extends CdcPipelineReaderProperties {
  private Integer pollingIntervalInMilliseconds = 500;
  private Integer maxEventsPerPolling = 1000;
  private Integer maxAttemptsForPolling = 100;
  private Integer pollingRetryIntervalInMilliseconds = 500;

  public Integer getPollingIntervalInMilliseconds() {
    return pollingIntervalInMilliseconds;
  }

  public void setPollingIntervalInMilliseconds(Integer pollingIntervalInMilliseconds) {
    this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
  }

  public Integer getMaxEventsPerPolling() {
    return maxEventsPerPolling;
  }

  public void setMaxEventsPerPolling(Integer maxEventsPerPolling) {
    this.maxEventsPerPolling = maxEventsPerPolling;
  }

  public Integer getMaxAttemptsForPolling() {
    return maxAttemptsForPolling;
  }

  public void setMaxAttemptsForPolling(Integer maxAttemptsForPolling) {
    this.maxAttemptsForPolling = maxAttemptsForPolling;
  }

  public Integer getPollingRetryIntervalInMilliseconds() {
    return pollingRetryIntervalInMilliseconds;
  }

  public void setPollingRetryIntervalInMilliseconds(Integer pollingRetryIntervalInMilliseconds) {
    this.pollingRetryIntervalInMilliseconds = pollingRetryIntervalInMilliseconds;
  }
}
