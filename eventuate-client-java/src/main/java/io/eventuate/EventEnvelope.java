package io.eventuate;

import io.eventuate.common.id.Int128;

import java.util.Map;
import java.util.Optional;

/**
 * A container of an event and it's metadata
 *
 * @param <T> the event class
 */
public interface EventEnvelope<T extends Event> {

  /**
   * The event
   * @return the event
   */
  T getEvent();

  /**
   * The event id
   * @return the event id
   */

  Int128 getEventId();

  /**
   * The event type
   * @return the event type
   */

  Class<T> getEventType();

  /**
   * The id of the aggregate that published the event
    * @return the entity id
   */
  String getEntityId();

  /**
   * Each aggregate (the sender) is assigned a swimlane, which can be used for concurrent event processing
   *
   * @return the swimlane
   */
  Integer getSwimlane();

  /**
   * A monotonically increasing offset within the swimlane
   * @return the offset
   */
  Long getOffset();

  /**
   * An opaque token that can used to update aggregates idempotently
   *
   * @return the context
   *
   * @see UpdateOptions
   * @see FindOptions
   */
  EventContext getEventContext();

  Optional<Map<String, String>> getEventMetadata();
}
