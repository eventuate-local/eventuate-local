package io.eventuate.javaclient.commonimpl.crud;

import io.eventuate.EntityIdAndVersion;
import io.eventuate.common.id.Int128;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

public class EntityIdVersionAndEventIds {

  private final String entityId;
  private final Int128 entityVersion;
  private final List<Int128> eventIds;

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

  public EntityIdVersionAndEventIds(String entityId, Int128 entityVersion, List<Int128> eventIds) {
    this.entityId = entityId;
    this.entityVersion = entityVersion;
    this.eventIds = eventIds;
  }

  public String getEntityId() {
    return entityId;
  }

  public List<Int128> getEventIds() {
    return eventIds;
  }

  public Int128 getEntityVersion() {
    return entityVersion;
  }

  public EntityIdAndVersion toEntityIdAndVersion() {
    return new EntityIdAndVersion(entityId, entityVersion);
  }
}
