package io.eventuate.javaclient.jdbc;

import io.eventuate.*;
import io.eventuate.javaclient.commonimpl.*;
import io.eventuate.javaclient.commonimpl.sync.AggregateEvents;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class EventuateEmbeddedTestAggregateStore extends AbstractJdbcAggregateCrud implements AggregateEvents {

  private final AtomicLong eventOffset = new AtomicLong();
  private final Map<String, List<Subscription>> aggregateTypeToSubscription = new HashMap<>();

  public EventuateEmbeddedTestAggregateStore(EventuateJdbcAccess eventuateJdbcAccess) {
    super(eventuateJdbcAccess);
  }

  @Override
  protected void publish(PublishableEvents publishableEvents) {
    String aggregateType = publishableEvents.getAggregateType();
    String aggregateId = publishableEvents.getEntityId();
    List<EventIdTypeAndData> eventsWithIds = publishableEvents.getEventsWithIds();
    List<Subscription> subscriptions;
    synchronized (aggregateTypeToSubscription) {
      List<Subscription> x = aggregateTypeToSubscription.get(aggregateType);
      subscriptions = x == null ? null : new ArrayList<>(x);
    }
    if (subscriptions != null)
      for (Subscription subscription : subscriptions) {
        for (EventIdTypeAndData event : eventsWithIds) {
          if (subscription.isInterestedIn(aggregateType, event.getEventType()))
            subscription.handler.apply(new SerializedEvent(event.getId(), aggregateId, aggregateType, event.getEventData(), event.getEventType(),
                    aggregateId.hashCode() % 8,
                    eventOffset.getAndIncrement(),
                    new EventContext(event.getId().asString()), event.getMetadata()));
        }
      }
  }


  class Subscription {

    private final String subscriberId;
    private final Map<String, Set<String>> aggregatesAndEvents;
    private final Function<SerializedEvent, CompletableFuture<?>> handler;

    public Subscription(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, Function<SerializedEvent, CompletableFuture<?>> handler) {

      this.subscriberId = subscriberId;
      this.aggregatesAndEvents = aggregatesAndEvents;
      this.handler = handler;
    }

    public boolean isInterestedIn(String aggregateType, String eventType) {
      return aggregatesAndEvents.get(aggregateType) != null && aggregatesAndEvents.get(aggregateType).contains(eventType);
    }
  }

  @Override
  public void subscribe(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, SubscriberOptions options, Function<SerializedEvent, CompletableFuture<?>> handler) {
    // TODO handle options
    Subscription subscription = new Subscription(subscriberId, aggregatesAndEvents, handler);
    synchronized (aggregateTypeToSubscription) {
      for (String aggregateType : aggregatesAndEvents.keySet()) {
        List<Subscription> existing = aggregateTypeToSubscription.get(aggregateType);
        if (existing == null) {
          existing = new LinkedList<>();
          aggregateTypeToSubscription.put(aggregateType, existing);
        }
        existing.add(subscription);
      }
    }
  }

}
