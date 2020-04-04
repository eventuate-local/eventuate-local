package io.eventuate;

import io.eventuate.common.id.Int128;
import org.apache.commons.lang.builder.ToStringBuilder;

public class EntityWithIdAndVersion<T> {
  private final EntityIdAndVersion entityIdAndVersion;
  private final T aggregate;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public EntityWithIdAndVersion(EntityIdAndVersion entityIdAndVersion, T aggregate) {

    this.entityIdAndVersion = entityIdAndVersion;
    this.aggregate = aggregate;
  }

  public EntityIdAndVersion getEntityIdAndVersion() {
    return entityIdAndVersion;
  }

  public T getAggregate() {
    return aggregate;
  }

  public String getEntityId() {
    return entityIdAndVersion.getEntityId();
  }

  public Int128 getEntityVersion() {
    return entityIdAndVersion.getEntityVersion();
  }
}
