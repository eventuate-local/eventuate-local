package io.eventuate;

import io.eventuate.common.id.Int128;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class EntityIdAndVersion {

  private final String entityId;
  private final Int128 entityVersion;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public boolean equals(Object o) {
    return EqualsBuilder.reflectionEquals(this, 0);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public EntityIdAndVersion(String entityId, Int128 entityVersion) {
    this.entityId = entityId;
    this.entityVersion = entityVersion;
  }

  public String getEntityId() {
    return entityId;
  }

  public Int128 getEntityVersion() {
    return entityVersion;
  }
}
