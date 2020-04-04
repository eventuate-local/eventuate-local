package io.eventuate.javaclient.commonimpl.schemametadata;

import io.eventuate.javaclient.commonimpl.EventIdTypeAndData;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class EventSchemaMetadataManagerImpl implements EventSchemaMetadataManager {
  @Override
  public Optional<String> currentVersion(Class clasz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<EventIdTypeAndData> upcastEvents(Class clasz, List<EventIdTypeAndData> events) {
    Optional<String> possibleVersion = currentVersion(clasz);
    if (possibleVersion.isPresent()) {
      return events.stream().map(event -> maybeUpcast(clasz, possibleVersion.get(), event)).collect(toList());
    } else {
      return events;
    }

  }

  private EventIdTypeAndData maybeUpcast(Class clasz, String latestVersion, EventIdTypeAndData event) {
    return eventVersion(event).map(currentVersion -> needsUpcast(latestVersion, currentVersion) ? upcast(event, latestVersion, currentVersion) : event).orElse(event);
  }

  private EventIdTypeAndData upcast(EventIdTypeAndData event, String latestVersion, String actualVersion) {
    throw new UnsupportedOperationException();
  }

  private Optional<String> eventVersion(EventIdTypeAndData event) {
    throw new UnsupportedOperationException();
  }

  private boolean needsUpcast(String latestVersion, String currentVersion) {
    throw new UnsupportedOperationException();
  }
}
