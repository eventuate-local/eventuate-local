package io.eventuate.testutil;

import io.eventuate.Aggregate;
import io.eventuate.Event;
import io.eventuate.EventEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractAggregateEventTest<A extends Aggregate<A>, T extends Event> {

  private Class<A> aggregateClass;
  private Class<T> eventClass;

  protected AbstractAggregateEventTest(Class<A> aggregateClass, Class<T> eventClass) {
    this.aggregateClass = aggregateClass;
    this.eventClass = eventClass;
  }

  @Test
  public void entityEventShouldReferenceAggregateClass() {
    assertEquals(aggregateClass.getName(), eventClass.getAnnotation(EventEntity.class).entity());
  }
}
