package io.eventuate;

import java.util.Map;
import java.util.Set;

public class RegisteredSubscription {
  private final String subscriberId;
  private final Map<String, Set<String>> aggregatesAndEvents;
  private final Class<?> eventSubscriberClass;

  public RegisteredSubscription(String subscriberId, Map<String, Set<String>> aggregatesAndEvents, Class<?> eventSubscriberClass) {

    this.subscriberId = subscriberId;
    this.aggregatesAndEvents = aggregatesAndEvents;
    this.eventSubscriberClass = eventSubscriberClass;
  }

  public String getSubscriberId() {
    return subscriberId;
  }

  public Map<String, Set<String>> getAggregatesAndEvents() {
    return aggregatesAndEvents;
  }

  public Class<?> getEventSubscriberClass() {
    return eventSubscriberClass;
  }
}
