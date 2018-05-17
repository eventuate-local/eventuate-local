package io.eventuate.local.common.status;

import java.util.LinkedList;
import java.util.List;

public class StatusService {
  private volatile CDCStatus status = CDCStatus.CONNECTING;
  private volatile long processedEvents;
  private volatile LinkedList<String> lastEvents = new LinkedList<>();

  public CDCStatus getStatus() {
    return status;
  }

  public List<String> getLastEvents() {
    return lastEvents;
  }

  public long getProcessedEvents() {
    return processedEvents;
  }

  public void markAsStarted() {
    status = CDCStatus.STARTED;
  }

  public void addPublishedEvent(String event) {
    status = CDCStatus.PUBLISHING;
    processedEvents++;
    lastEvents.add(event);

    while (lastEvents.size() > 10) {
        lastEvents.poll();
    }
  }
}
