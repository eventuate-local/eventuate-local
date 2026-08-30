package io.eventuate;

import io.eventuate.common.id.Int128;

public class OptimisticLockingException extends EventuateClientException {

  private EntityIdAndType entityIdAndType;
  private Int128 entityVersion;

  public OptimisticLockingException(EntityIdAndType entityIdAndType, Int128 entityVersion) {
    super("Couldn't update entity: %s, %s, %s".formatted(entityIdAndType.getEntityType(), entityIdAndType.getEntityId(), entityVersion));
    this.entityIdAndType = entityIdAndType;
    this.entityVersion = entityVersion;
  }

  public OptimisticLockingException() {
  }

  public EntityIdAndType getEntityIdAndType() {
    return entityIdAndType;
  }

  public Int128 getEntityVersion() {
    return entityVersion;
  }
}

