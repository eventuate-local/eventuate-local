package io.eventuate.local.common;

public interface CdcProcessingStatusService {
  CdcProcessingStatus getCurrentStatus();
  void saveEndingOffsetOfLastProcessedEvent(long endingOffsetOfLastProcessedEvent);
}