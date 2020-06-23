package io.eventuate.javaclient.commonimpl.schema;

import com.fasterxml.jackson.databind.JsonNode;

public interface EventUpcaster {
  JsonNode upcast(JsonNode json);
}
