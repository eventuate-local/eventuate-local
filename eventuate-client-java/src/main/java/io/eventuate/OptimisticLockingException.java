package io.eventuate;

import io.eventuate.common.id.Int128;

public class OptimisticLockingException extends EventuateClientException {

  private EntityIdAndType entityIdAndType;
  private Int128 entityVersion;

  public OptimisticLockingException(EntityIdAndType entityIdAndType, Int128 entityVersion) {
    super(String.format("Couldn't update entity: %s, %s, %s", entityIdAndType.getEntityType(), entityIdAndType.getEntityId(), entityVersion));
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

