package io.eventuate.sync;

import io.eventuate.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Wraps the event that is passed to a command-side event handler
 * @param <T> the type of the event
 */
public interface EventHandlerContext<T extends Event> extends EventEnvelope<T> {

  /**
   * Creates an aggregate in response to an event
   *
   * @param entityClass the class of the aggregate to create, must be a subclass of CommandProcessingAggregate
   * @param command the command to process
   * @param entityId the optional id of the aggregate to create
   * @param <A> the aggregate class
   * @param <CT> the command class
   * @return the newly created and persisted aggregate
   */
  <A extends CommandProcessingAggregate<A, CT>, CT extends Command> CompletableFuture<EntityWithIdAndVersion<A>> save(Class<A> entityClass, CT command, Optional<String> entityId);

  /**
   * Updates an aggregate in response to an event
   * @param entityClass the class of the aggregate to create, must be a subclass of CommandProcessingAggregate
   * @param command the command to process
   * @param entityId the optional id of the aggregate to create
   * @param <A> the aggregate class
   * @param <CT> the command class
   * @return the updated and persisted aggregate
   */
  <A extends CommandProcessingAggregate<A, CT>, CT extends Command> CompletableFuture<EntityWithIdAndVersion<A>> update(Class<A> entityClass, String entityId, CT command);

  /**
   * Updates an aggregate in response to an event
   * @param entityClass the class of the aggregate to create, must be a subclass of CommandProcessingAggregate
   * @param commandProvider provides the command to process
   * @param entityId the optional id of the aggregate to create
   * @param <A> the aggregate class
   * @param <CT> the command class
   * @return the updated and persisted aggregate
   */
  <A extends CommandProcessingAggregate<A, CT>, CT extends Command> CompletableFuture<EntityWithIdAndVersion<A>>
      updateWithProvidedCommand(Class<A> entityClass, String entityId, Function<A, Optional<CT>> commandProvider);

}
