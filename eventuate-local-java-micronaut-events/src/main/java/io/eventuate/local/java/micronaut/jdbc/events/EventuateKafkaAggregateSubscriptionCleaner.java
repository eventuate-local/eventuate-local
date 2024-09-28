package io.eventuate.local.java.micronaut.jdbc.events;

import io.eventuate.local.java.events.EventuateKafkaAggregateSubscriptions;
import io.micronaut.context.annotation.Context;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Arrays;

@Context
public class EventuateKafkaAggregateSubscriptionCleaner {
  @Inject
  private EventuateKafkaAggregateSubscriptions[] eventuateKafkaAggregateSubscriptions;

  @PreDestroy
  public void clean() {
    Arrays.stream(eventuateKafkaAggregateSubscriptions).forEach(EventuateKafkaAggregateSubscriptions::cleanUp);
  }

}
