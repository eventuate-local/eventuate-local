package io.eventuate.testutil;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import io.eventuate.EventHandlerContext;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static io.eventuate.testutil.Eventually.eventuallyReturning;


public class EventTracker {

  private BlockingQueue<ReceivedEvent> events = new LinkedBlockingQueue<>();
  private int eventTrackerInterval;
  private int eventTrackerIterations;

  public EventTracker() {
    eventTrackerInterval = Optional.ofNullable(System.getenv("EVENTUATE_EVENT_TRACKER_INTERVAL_IN_SECONDS")).map(Integer::parseInt).orElse(1);
    eventTrackerIterations = Optional.ofNullable(System.getenv("EVENTUATE_EVENT_TRACKER_ITERATIONS")).map(Integer::parseInt).orElse(30);
  }

  public BlockingQueue<ReceivedEvent> getEvents() {
    return events;
  }


  public <T extends Event> void add(DispatchedEvent<T> ctx) {
    events.add(new ReceivedEvent(ctx.getEntityId(), (Class<Event>)ctx.getEventType(), ctx.getEventId(), ctx.getEvent(), ctx.getEventMetadata()));
  }

  public <T extends Event> void add(EventHandlerContext<T> ctx) {
    events.add(new ReceivedEvent(ctx.getEntityId(), (Class<Event>)ctx.getEventType(), ctx.getEventId(), ctx.getEvent(), ctx.getEventMetadata()));
  }

  public <T extends Event> ReceivedEvent assertMessagePublished(String entityId, Class<T> eventClass) {
    Predicate<ReceivedEvent> predicate = event -> event.getEntityId().equals(entityId) && event.getEventType().equals(eventClass);
    String message = String.format("Haven't found event from %s of type %s", entityId, eventClass);
    return assertMessagePublished(message, predicate);
  }

  public ReceivedEvent assertMessagePublished(String message, Predicate<ReceivedEvent> predicate) {
    return eventuallyReturning(eventTrackerIterations, eventTrackerInterval, TimeUnit.SECONDS, () -> {
      ReceivedEvent[] currentEvents = events.toArray(new ReceivedEvent[events.size()]);
      for (ReceivedEvent event : currentEvents) {
        if (predicate.test(event)) {
          return event;
        }
      }
      throw new RuntimeException(message);
    });
  }
}
