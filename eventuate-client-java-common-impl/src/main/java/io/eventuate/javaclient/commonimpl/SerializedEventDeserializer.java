package io.eventuate.javaclient.commonimpl;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;

import java.util.Optional;

public interface SerializedEventDeserializer {
  Optional<DispatchedEvent<Event>> toDispatchedEvent(SerializedEvent se);
}
