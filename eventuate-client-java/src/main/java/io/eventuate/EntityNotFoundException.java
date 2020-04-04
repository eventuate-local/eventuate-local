package io.eventuate;

public class EntityNotFoundException extends EventuateClientException {
  public EntityNotFoundException() {
  }

  public EntityNotFoundException(String aggregateType, String entityId) {
    super(String.format("aggregateType: %s, entityId: %s", aggregateType, entityId));
  }
}
