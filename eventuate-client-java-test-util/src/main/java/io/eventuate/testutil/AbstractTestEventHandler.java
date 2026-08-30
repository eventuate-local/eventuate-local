package io.eventuate.testutil;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import io.eventuate.EventHandlerContext;
import io.eventuate.common.id.Int128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public abstract class AbstractTestEventHandler {
  protected EventTracker eventTracker = new EventTracker();
  protected Logger logger = LoggerFactory.getLogger(getClass());


  public <T extends Event> void add(DispatchedEvent<T> ctx) {
    eventTracker.add(ctx);
  }

  public <T extends Event> void add(EventHandlerContext<T> ctx) {
    eventTracker.add(ctx);
  }

  public <T extends Event> ReceivedEvent assertMessagePublished(String entityId, Class<T> eventClass) {
    return eventTracker.assertMessagePublished(entityId, eventClass);
  }

  public ReceivedEvent assertMessagePublished(String message, Predicate<ReceivedEvent> predicate) {
    return eventTracker.assertMessagePublished(message, predicate);
  }

  public ReceivedEvent eventuallyContains(String message, Predicate<ReceivedEvent> predicate) {
    return assertMessagePublished(message, predicate);
  }

  public ReceivedEvent eventuallyContains(String message, Int128 eventId) {
    return assertMessagePublished(message + " looking for eventid " + eventId.asString(), (m) -> m.getEventId().equals(eventId));
  }
}
