package io.eventuate.javaclient.commonimpl.schemametadata;

import io.eventuate.javaclient.commonimpl.EventIdTypeAndData;

import java.util.List;
import java.util.Optional;

public class EmptyEventSchemaMetadataManager implements EventSchemaMetadataManager {
  @Override
  public Optional<String> currentVersion(Class clasz) {
    return Optional.empty();
  }

  @Override
  public List<EventIdTypeAndData> upcastEvents(Class clasz, List<EventIdTypeAndData> events) {
    return events;
  }
}
