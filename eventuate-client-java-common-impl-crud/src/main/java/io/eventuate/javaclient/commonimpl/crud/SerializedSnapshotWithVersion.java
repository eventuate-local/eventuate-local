package io.eventuate.javaclient.commonimpl.crud;

import io.eventuate.common.id.Int128;

public class SerializedSnapshotWithVersion {

  private SerializedSnapshot serializedSnapshot;
  private Int128 entityVersion;

  public SerializedSnapshotWithVersion(SerializedSnapshot serializedSnapshot, Int128 entityVersion) {
    this.serializedSnapshot = serializedSnapshot;
    this.entityVersion = entityVersion;
  }

  public SerializedSnapshot getSerializedSnapshot() {
    return serializedSnapshot;
  }

  public Int128 getEntityVersion() {
    return entityVersion;
  }
}
