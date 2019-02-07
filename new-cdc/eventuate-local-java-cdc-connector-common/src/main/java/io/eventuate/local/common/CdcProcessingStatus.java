package io.eventuate.local.common;

import org.apache.commons.lang.builder.ToStringBuilder;

public class CdcProcessingStatus {
  private long lastEventOffset;
  private long logsHighwaterMark;
  private boolean cdcProcessingFinished;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public CdcProcessingStatus(long lastEventOffset, long logsHighwaterMark) {
    this(lastEventOffset, logsHighwaterMark, lastEventOffset == logsHighwaterMark);
  }

  public CdcProcessingStatus(long lastEventOffset, long logsHighwaterMark, boolean cdcProcessingFinished) {
    this.lastEventOffset = lastEventOffset;
    this.logsHighwaterMark = logsHighwaterMark;
    this.cdcProcessingFinished = cdcProcessingFinished;
  }

  public long getLastEventOffset() {
    return lastEventOffset;
  }

  public long getLogsHighwaterMark() {
    return logsHighwaterMark;
  }

  public boolean isCdcProcessingFinished() {
    return cdcProcessingFinished;
  }
}