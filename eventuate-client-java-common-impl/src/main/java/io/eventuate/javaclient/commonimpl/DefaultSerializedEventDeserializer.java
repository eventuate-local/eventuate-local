package io.eventuate.javaclient.commonimpl;

import io.eventuate.DispatchedEvent;
import io.eventuate.EndOfCurrentEventsReachedEvent;
import io.eventuate.Event;
import io.eventuate.common.json.mapper.JSonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class DefaultSerializedEventDeserializer implements SerializedEventDeserializer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public Optional<DispatchedEvent<Event>> toDispatchedEvent(SerializedEvent se) {
    String eventType = se.getEventType();
    Class<Event> eventClass = toEventClass(eventType);

    Event event = JSonMapper.fromJson(se.getEventData(), eventClass);
    return Optional.of(new DispatchedEvent<>(se.getEntityId(),
            se.getId(),
            event,
            se.getSwimLane(),
            se.getOffset(), se.getEventContext(),
            se.getMetadata() == null ? Optional.empty() : se.getMetadata().map(md -> JSonMapper.fromJson(md, Map.class))));
  }

  private Class<Event> toEventClass(String eventType) {
    if ("net.chrisrichardson.eventstore.subscriptions.EndOfCurrentEventsReachedEvent".equals(eventType)) {
      eventType = EndOfCurrentEventsReachedEvent.class.getName();
    }
    try {
      return (Class<Event>) Class.forName(eventType);
    } catch (ClassNotFoundException e) {
      logger.error("Event class not found", e);
      throw new RuntimeException(e);
    }
  }

}
