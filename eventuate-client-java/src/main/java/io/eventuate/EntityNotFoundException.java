package io.eventuate;

public class EntityNotFoundException extends EventuateClientException {
  public EntityNotFoundException() {
  }

  public EntityNotFoundException(String aggregateType, String entityId) {
    super("aggregateType: %s, entityId: %s".formatted(aggregateType, entityId));
  }
}
