package io.eventuate.javaclient.commonimpl.events;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import io.eventuate.javaclient.commonimpl.common.SerializedEvent;

import java.util.Optional;

public interface SerializedEventDeserializer {
  Optional<DispatchedEvent<Event>> toDispatchedEvent(SerializedEvent se);
}
