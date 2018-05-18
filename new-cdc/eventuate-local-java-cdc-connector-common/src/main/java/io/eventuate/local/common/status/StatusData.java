package io.eventuate.local.common.status;

import java.util.List;

public class StatusData {
  private CDCStatus status;
  private long processedEvents;
  private List<String> lastProcessedEvents;

  public StatusData() {
  }

  public StatusData(CDCStatus status, long processedEvents, List<String> lastProcessedEvents) {
    this.status = status;
    this.processedEvents = processedEvents;
    this.lastProcessedEvents = lastProcessedEvents;
  }

  public CDCStatus getStatus() {
    return status;
  }

  public void setStatus(CDCStatus status) {
    this.status = status;
  }

  public long getProcessedEvents() {
    return processedEvents;
  }

  public void setProcessedEvents(long processedEvents) {
    this.processedEvents = processedEvents;
  }

  public List<String> getLastProcessedEvents() {
    return lastProcessedEvents;
  }

  public void setLastProcessedEvents(List<String> lastProcessedEvents) {
    this.lastProcessedEvents = lastProcessedEvents;
  }
}
