package io.eventuate.javaclient.commonimpl.schema;

import io.eventuate.javaclient.commonimpl.EventIdTypeAndData;
import io.eventuate.javaclient.commonimpl.SerializedEvent;

import java.util.List;
import java.util.Map;

public interface EventuateEventSchemaManager {

  Map<String, String> currentSchemaMetadata(String clasz);

  List<EventIdTypeAndData> upcastEvents(String aggregateType, List<EventIdTypeAndData> events);

  SerializedEvent upcastEvent(SerializedEvent se);
}
