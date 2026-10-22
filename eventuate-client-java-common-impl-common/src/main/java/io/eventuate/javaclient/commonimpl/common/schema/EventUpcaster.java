package io.eventuate.javaclient.commonimpl.common.schema;

import tools.jackson.databind.JsonNode;

public interface EventUpcaster {
  JsonNode upcast(JsonNode json);
}
