package io.eventuate;

public class EventuateRestoreFromSnapshotFailedUnexpectedlyException extends EventuateClientException {
  public EventuateRestoreFromSnapshotFailedUnexpectedlyException(ReflectiveOperationException e) {
    super(e);
  }
}
