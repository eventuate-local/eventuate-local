package io.eventuate.javaclient.commonimpl.schema;

import java.util.List;
import java.util.Optional;

public class AggregateSchemaVersion {
  private final String version;
  private final List<EventRename> renames;
  private final List<EventTransform> transforms;

  public AggregateSchemaVersion(String version, List<EventRename> renames, List<EventTransform> transforms) {
    this.version = version;
    this.renames = renames;
    this.transforms = transforms;
  }

  public String getVersion() {
    return version;
  }

  public Optional<EventUpcaster> findUpcaster(String eventType) {
    return transforms.stream().filter(t -> t.isFor(eventType)).findAny().map(EventTransform::getUpcaster);
  }

  public String maybeRename(String eventType) {
    return renames.stream().filter(rn -> rn.isFor(eventType)).findAny().map(EventRename::getNewEventName).orElse(eventType);
  }
}
