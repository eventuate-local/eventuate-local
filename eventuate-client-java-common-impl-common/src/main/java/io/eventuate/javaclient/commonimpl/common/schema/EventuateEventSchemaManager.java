package io.eventuate.javaclient.commonimpl.common.schema;

import io.eventuate.javaclient.commonimpl.common.EventIdTypeAndData;
import io.eventuate.javaclient.commonimpl.common.SerializedEvent;

import java.util.List;
import java.util.Map;

public interface EventuateEventSchemaManager {

  Map<String, String> currentSchemaMetadata(String clasz);

  List<EventIdTypeAndData> upcastEvents(String aggregateType, List<EventIdTypeAndData> events);

  SerializedEvent upcastEvent(SerializedEvent se);
}
