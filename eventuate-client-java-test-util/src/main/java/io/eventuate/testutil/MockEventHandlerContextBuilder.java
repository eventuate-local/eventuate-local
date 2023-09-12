package io.eventuate.testutil;

import io.eventuate.Command;
import io.eventuate.CommandProcessingAggregate;
import io.eventuate.Event;

import java.util.Optional;

public class MockEventHandlerContextBuilder {
  public static  <E extends Event> MockEventHandlerContextBuilderWithEvent<E> forEvent(E event) {
    return new MockEventHandlerContextBuilderWithEvent<E>(event);

  }

  public static class MockEventHandlerContextBuilderWithEvent<E extends Event> {
    private E event;
    private String eventFromEntityId;

    public MockEventHandlerContextBuilderWithEvent(E event) {
      this.event = event;
    }

    public MockEventHandlerContextBuilderWithEvent<E> fromEntityId(String entityId) {
      this.eventFromEntityId = entityId;
      return this;
    }

    public <T extends CommandProcessingAggregate<T, CT>, CT extends Command, C extends CT> MockEventHandlerContext<T, CT, E, C>
    expectUpdate(Class<T> aggregateClass, String entityId, Class<C> commandClass) {

      MockEventHandlerContextInternal<T, CT, E> mehc = new MockEventHandlerContextInternal<>(aggregateClass);
      mehc.withEvent(event);
      mehc.withFromEntityId(eventFromEntityId);
      AggregateOperationInvocation<T, CT, C> aggregateOperationInvocation = mehc.whenUpdate(entityId, commandClass);
      return new MockEventHandlerContext<>(aggregateOperationInvocation.getEntity(), aggregateOperationInvocation, mehc.mock);
    }

    public <T extends CommandProcessingAggregate<T, CT>, CT extends Command, C extends CT> MockEventHandlerContext<T, CT, E, C> expectSave(Class<T> aggregateClass, Class<C> commandClass, Optional<String> entityId) {
      MockEventHandlerContextInternal<T, CT, E> mehc = new MockEventHandlerContextInternal<>(aggregateClass);
      mehc.withEvent(event);
      mehc.withFromEntityId(eventFromEntityId);
      SaveInvocation<T, CT, C> saveInvocation = mehc.whenSave(commandClass, entityId);
      return new MockEventHandlerContext<>(saveInvocation.getEntity(), saveInvocation, mehc.mock);
    }
  }
}
