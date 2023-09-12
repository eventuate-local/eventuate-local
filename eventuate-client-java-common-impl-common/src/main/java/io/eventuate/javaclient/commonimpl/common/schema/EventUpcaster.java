package io.eventuate.javaclient.commonimpl.common.schema;

import com.fasterxml.jackson.databind.JsonNode;

public interface EventUpcaster {
  JsonNode upcast(JsonNode json);
}
