package io.eventuate.javaclient.commonimpl.common.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.javaclient.commonimpl.common.EventIdTypeAndData;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class AggregateSchema {
  private final String aggregateType;
  private final List<AggregateSchemaVersion> versions;

  public AggregateSchema(String aggregateType, List<AggregateSchemaVersion> versions) {
    this.aggregateType = aggregateType;
    this.versions = versions;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public String currentVersion() {
    return versions.get(versions.size() - 1).getVersion();
  }

  public List<EventIdTypeAndData> upcastEvents(List<EventIdTypeAndData> events) {
    String currentVersion = currentVersion();
    return events.stream().map(event -> maybeUpcast(currentVersion, event)).collect(toList());
  }

  private EventIdTypeAndData maybeUpcast(String latestVersion, EventIdTypeAndData event) {
    String actualVersion = eventVersion(event);
    return needsUpcast(latestVersion, actualVersion) ? upcast(event, latestVersion, actualVersion) : event;
  }

  private EventIdTypeAndData upcast(EventIdTypeAndData event, String toVersion, String fromVersion) {
    String originalEventType = event.getEventType();
    NewEventNameAndUpcasters newEventTypeAndUpcasters = findUpcasters(originalEventType, fromVersion, toVersion);

    if (newEventTypeAndUpcasters.isEmpty())
      return event;

    JsonNode json;
    try {
      json = JSonMapper.objectMapper.readTree(event.getEventData());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    for (EventUpcaster upcaster : newEventTypeAndUpcasters.getUpcasters())
      json = upcaster.upcast(json);


    String newJson;
    try {
      newJson = JSonMapper.objectMapper.writeValueAsString(json);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return new EventIdTypeAndData(event.getId(), newEventTypeAndUpcasters.getEventType().orElse(originalEventType), newJson, withNewVersion(event.getMetadata(), currentVersion()));

  }


  private NewEventNameAndUpcasters findUpcasters(String eventType, String fromVersion, String toVersion) {
    int versionIndex = 0;
    String originalEventType = eventType;
    while (fromVersion != null && !versions.get(versionIndex++).getVersion().equals(fromVersion)) {
      // keep looking
    }
    List<EventUpcaster> upcasters = new ArrayList<>();

    for (; versionIndex < versions.size() ; versionIndex++) {
      AggregateSchemaVersion aggregateSchemaVersion = versions.get(versionIndex);
      eventType = aggregateSchemaVersion.maybeRename(eventType);
      Optional<EventUpcaster> upcaster = aggregateSchemaVersion.findUpcaster(eventType);
      upcaster.ifPresent(upcasters::add);
    }
    return new NewEventNameAndUpcasters(eventType.equals(originalEventType) ? Optional.empty() : Optional.of(eventType), upcasters);
  }

  private String eventVersion(EventIdTypeAndData event) {
    Map map = event.getMetadata().map(md -> JSonMapper.fromJson(md, Map.class)).orElse(Collections.EMPTY_MAP);
    return (String) map.get(DefaultEventuateEventSchemaManager.SCHEMA_VERSION);
  }

  private Optional<String> withNewVersion(Optional<String> metadata, String currentVersion) {
    Map map = metadata.map(md -> JSonMapper.fromJson(md, Map.class)).orElse(new HashMap());
    map.put(DefaultEventuateEventSchemaManager.SCHEMA_VERSION, currentVersion);
    return Optional.of(JSonMapper.toJson(map));
  }

  private boolean needsUpcast(String latestVersion, String actualVersion) {
    return !Objects.equals(latestVersion, actualVersion);
  }

}
