package io.eventuate.javaclient.commonimpl.schemametadata;

import io.eventuate.javaclient.commonimpl.EventIdTypeAndData;

import java.util.List;
import java.util.Optional;

public interface EventSchemaMetadataManager {

  Optional<String> currentVersion(Class clasz);

  List<EventIdTypeAndData> upcastEvents(Class clasz, List<EventIdTypeAndData> events);
}
