package io.eventuate.local.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.publisher.changes.EventRecord;

import java.io.IOException;

public class EventRecordDeserializer {

  public static EventRecord toEventRecord(String json) {
    ObjectMapper om = new ObjectMapper();
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      return om.readValue(json, EventRecord.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String toJson(PublishedEvent eventInfo) {
    ObjectMapper om = new ObjectMapper();
    try {
      return om.writeValueAsString(eventInfo);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
