package io.eventuate.local.common;

public class CdcProcessingStatus {
  private long lastEventOffset;
  private long logsHighwaterMark;
  private boolean cdcProcessingFinished;

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