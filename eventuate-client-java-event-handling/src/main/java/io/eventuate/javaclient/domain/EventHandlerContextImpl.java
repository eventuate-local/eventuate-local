package io.eventuate.javaclient.domain;

import io.eventuate.*;
import io.eventuate.common.id.Int128;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class EventHandlerContextImpl implements EventHandlerContext<Event> {
  private EventuateAggregateStoreCrud aggregateStore;
  private final DispatchedEvent<Event> de;

  public EventHandlerContextImpl(EventuateAggregateStoreCrud aggregateStore, DispatchedEvent<Event> de) {
    this.aggregateStore = aggregateStore;
    this.de = de;
  }

  @Override
  public Event getEvent() {
    return de.getEvent();
  }


  @Override
  public Int128 getEventId() {
    return de.getEventId();
  }

  @Override
  public Class<Event> getEventType() {
    return de.getEventType();
  }

  @Override
  public String getEntityId() {
    return de.getEntityId();
  }

  @Override
  public Integer getSwimlane() {
    return de.getSwimlane();
  }

  @Override
  public Long getOffset() {
    return de.getOffset();
  }

  public EventContext getEventContext() {
    return de.getEventContext();
  }

  @Override
  public Optional<Map<String, String>> getEventMetadata() {
    return de.getEventMetadata();
  }


  @Override
  public <U extends CommandProcessingAggregate<U, CT>, CT extends Command> CompletableFuture<EntityWithIdAndVersion<U>> save(Class<U> entityClass, CT command, Optional<String> entityId) {
    AggregateRepository<U, CT> ar = new AggregateRepository<>(entityClass, aggregateStore);
    return ar.save(command, Optional.of(new SaveOptions().withId(entityId).withEventContext(de.getEventContext())));
  }

  @Override
  public <U extends CommandProcessingAggregate<U, CT>, CT extends Command> CompletableFuture<EntityWithIdAndVersion<U>> update(Class<U> entityClass, String entityId, CT command) {
    AggregateRepository<U, CT> ar = new AggregateRepository<>(entityClass, aggregateStore);
    return ar.update(entityId, command, Optional.of(new UpdateOptions().withTriggeringEvent(de.getEventContext())));
  }

  @Override
  public <A extends CommandProcessingAggregate<A, CT>, CT extends Command> CompletableFuture<EntityWithIdAndVersion<A>>
    updateWithProvidedCommand(Class<A> entityClass, String entityId, Function<A, Optional<CT>> commandProvider) {
      AggregateRepository<A, CT> ar = new AggregateRepository<>(entityClass, aggregateStore);
      return ar.updateWithProvidedCommand(entityId, commandProvider, Optional.of(new UpdateOptions().withTriggeringEvent(de.getEventContext())));
  }
}
