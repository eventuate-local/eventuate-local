package io.eventuate.testutil;

import io.eventuate.*;
import io.eventuate.common.id.Int128;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class MockEventHandlerContextInternal<T extends CommandProcessingAggregate<T, CT>, CT extends Command, E extends Event> {

  public EventHandlerContext<E> mock = org.mockito.Mockito.mock(EventHandlerContext.class);
  private Class<T> aggregateClass;

  private String fromEntityId;
  private E event;
  private Int128 eventId;
  private Class<E> eventType;

  public MockEventHandlerContextInternal(Class<T> aggregateClass) {
    this.aggregateClass = aggregateClass;
  }

  public MockEventHandlerContextInternal<T, CT, E> withFromEntityId(String fromEntityId) {
    this.fromEntityId = fromEntityId;
    return this;
  }
  public MockEventHandlerContextInternal<T, CT, E> withEvent(E event) {
    this.event = event;
    return this;
  }

  public MockEventHandlerContextInternal<T, CT, E> withEventId(Int128 eventId) {
    this.eventId = eventId;
    return this;
  }

  public MockEventHandlerContextInternal<T, CT, E> withEventType(Class<E> eventType) {
    this.eventType = eventType;
    return this;
  }

  public <C extends CT> AggregateOperationInvocation<T,CT, C> whenUpdate(String entityId, Class<C> commandClass) {
    ArgumentCaptor<C> commandArg = ArgumentCaptor.forClass(commandClass);
    EntityWithIdAndVersion<T> updatedEntity = new EntityWithIdAndVersion<>(null, null);

    when(mock.getEntityId()).thenReturn(fromEntityId);
    when(mock.getEvent()).thenReturn(event);
    when(mock.getEventId()).thenReturn(eventId);
    when(mock.getEventType()).thenReturn(eventType);

    when(mock.update(eq(aggregateClass), eq(entityId), commandArg.capture())).thenReturn(CompletableFuture.completedFuture(updatedEntity));

    return new UpdateInvocation<>(commandArg, updatedEntity);
  }

  public <C extends CT> SaveInvocation<T, CT, C> whenSave(Class<C> commandClass, Optional<String> entityId) {
    ArgumentCaptor<C> commandArg = ArgumentCaptor.forClass(commandClass);
    EntityWithIdAndVersion<T> createdEntity = new EntityWithIdAndVersion<>(null, null);

    when(mock.getEntityId()).thenReturn(fromEntityId);
    when(mock.getEvent()).thenReturn(event);
    when(mock.getEventId()).thenReturn(eventId);
    when(mock.getEventType()).thenReturn(eventType);

    when(mock.save(eq(aggregateClass), commandArg.capture(), eq(entityId))).thenReturn(CompletableFuture.completedFuture(createdEntity));

    return new SaveInvocation<>(commandArg, createdEntity);
  }
}
