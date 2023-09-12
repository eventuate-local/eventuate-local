package io.eventuate.javaclient.commonimpl.common.schema;

import io.eventuate.javaclient.commonimpl.common.EventIdTypeAndData;
import io.eventuate.javaclient.commonimpl.common.SerializedEvent;

import java.util.*;

public class DefaultEventuateEventSchemaManager implements EventuateEventSchemaManager {

  public static final String SCHEMA_VERSION = "eventuate_schema_version";

  private Map<String, AggregateSchema> aggregateSchemaVersions = new HashMap<>();

  public void add(AggregateSchema aggregateSchema) {
    if (aggregateSchemaVersions.containsKey(aggregateSchema.getAggregateType()))
      throw new RuntimeException("Already defined: " + aggregateSchema.getAggregateType());
    aggregateSchemaVersions.put(aggregateSchema.getAggregateType(), aggregateSchema);
  }

  public Optional<String> currentVersion(String aggregateClass) {
    return Optional.ofNullable(aggregateSchemaVersions.get(aggregateClass)).map(AggregateSchema::currentVersion);
  }

  @Override
  public Map<String, String> currentSchemaMetadata(String clasz) {
    return currentVersion(clasz).map(currentVersion -> Collections.singletonMap(SCHEMA_VERSION, currentVersion)).orElseGet(Collections::emptyMap);
  }

  @Override
  public List<EventIdTypeAndData> upcastEvents(String aggregateType, List<EventIdTypeAndData> events) {
    return Optional.ofNullable(aggregateSchemaVersions.get(aggregateType)).map(as -> as.upcastEvents(events)).orElse(events);
  }

  @Override
  public SerializedEvent upcastEvent(SerializedEvent se) {
    EventIdTypeAndData original = new EventIdTypeAndData(se.getId(), se.getEventType(), se.getEventData(), se.getMetadata() == null ? Optional.empty() : se.getMetadata());
    EventIdTypeAndData upcasted = upcastEvents(se.getEntityType(), Collections.singletonList(original)).get(0);
    SerializedEvent newSe = new SerializedEvent(se.getId(), se.getEntityId(), se.getEntityType(), upcasted.getEventData(), upcasted.getEventType(), se.getSwimLane(), se.getOffset(), se.getEventContext(), upcasted.getMetadata());
    return newSe;
  }


}
