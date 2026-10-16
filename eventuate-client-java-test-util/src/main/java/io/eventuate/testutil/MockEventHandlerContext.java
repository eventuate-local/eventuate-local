package io.eventuate.testutil;

import io.eventuate.*;

/**
 *
 * @param <T> The aggregate type
 * @param <CT> The aggregate's command type
 * @param <E> The event that will be processed
 * @param <C> The concrete command type that will be processed
 */
public class MockEventHandlerContext<T extends CommandProcessingAggregate<T, CT>, CT extends Command, E extends Event, C extends CT> {
  private final AggregateOperationInvocation<T,CT, C> aggregateOperationInvocation;
  public final EntityWithIdAndVersion<T> updatedEntity;
  public final EventHandlerContext<E> mock;

  public MockEventHandlerContext(EntityWithIdAndVersion<T> updatedEntity, AggregateOperationInvocation<T, CT, C> aggregateOperationInvocation, EventHandlerContext<E> mock) {
    this.updatedEntity = updatedEntity;
    this.aggregateOperationInvocation = aggregateOperationInvocation;
    this.mock = mock;
  }

  public C getCommand() {
    return aggregateOperationInvocation.getCommand();
  }

  public static <E extends Event> MockEventHandlerContextBuilder.MockEventHandlerContextBuilderWithEvent<E> forEvent(E event) {
     return MockEventHandlerContextBuilder.forEvent(event);
  }

}