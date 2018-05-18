package io.eventuate.local.common.status;

import java.util.ArrayList;
import java.util.LinkedList;

public class StatusService {
  private CDCStatus status = CDCStatus.STARTING;
  private long processedEvents;
  private LinkedList<String> lastProcessedEvents = new LinkedList<>();

  public synchronized void markAsStarted() {
    status = CDCStatus.STARTED;
  }

  public synchronized void addPublishedEvent(String event) {
    processedEvents++;
    lastProcessedEvents.add(event);

    if (lastProcessedEvents.size() == 11) {
      lastProcessedEvents.poll();
    }
  }

  public synchronized StatusData getStatusData() {
    return new StatusData(status, processedEvents, new ArrayList<>(lastProcessedEvents));
  }
}
