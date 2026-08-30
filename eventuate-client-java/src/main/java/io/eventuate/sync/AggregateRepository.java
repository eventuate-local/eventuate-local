package io.eventuate.sync;


import io.eventuate.*;
import io.eventuate.common.id.Int128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A convenience class, with a synchronous-style API that provides a simplified interface for creating and updating aggregates.
 *
 * @param <T>  the aggregate class, which is a subtype of CommandProcessingAggregate
 * @param <CT> the aggregate's command class, a subtype of command
 *             <p>For example:
 *             <pre class="code">
 *             public class AccountService {
 *             private final AggregateRepository&gt;Account, AccountCommand&gt; accountRepository;
 *             public AccountService(AggregateRepository&gt;Account, AccountCommand&gt; accountRepository) {
 *             this.accountRepository = accountRepository;
 *             }
 *             public EntityWithIdAndVersion&gt;Account&gt; openAccount(BigDecimal initialBalance) {
 *             return accountRepository.save(new CreateAccountCommand(initialBalance));
 *             }
 *             }
 *             </pre>
 * @see CommandProcessingAggregate
 * @see Command
 */

public class AggregateRepository<T extends CommandProcessingAggregate<T, CT>, CT extends Command> extends CommonAggregateRepository<T, CT> {

  private static Logger logger = LoggerFactory.getLogger(AggregateRepository.class);

  private EventuateAggregateStoreCrud aggregateStore;


  /**
   * Constructs a new AggregateRepository for the specified aggregate class and aggregate store
   *
   * @param clasz          the class of the aggregate
   * @param aggregateStore the aggregate store
   */
  public AggregateRepository(Class<T> clasz, EventuateAggregateStoreCrud aggregateStore) {
    super(clasz);
    this.aggregateStore = aggregateStore;
  }

  /**
   * Create a new Aggregate by processing a command and persisting the events
   *
   * @param cmd the command to process
   * @return the newly persisted aggregate
   */
  public EntityWithIdAndVersion<T> save(CT cmd) {
    return save(cmd, Optional.empty());
  }

  /**
   * Create a new Aggregate by processing a command and persisting the events
   *
   * @param cmd         the command to process
   * @param saveOptions creation options
   * @return the newly persisted aggregate
   */
  public EntityWithIdAndVersion<T> save(CT cmd, Optional<SaveOptions> saveOptions) {
    T aggregate;
    try {
      aggregate = clasz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    List<Event> events = aggregate.processCommand(cmd);
    Aggregates.applyEventsToMutableAggregate(aggregate, events, missingApplyEventMethodStrategy);

    return new EntityWithIdAndVersion<>(aggregateStore.save(clasz, events, saveOptions), aggregate);
  }

  /**
   * Update the specified aggregate by processing a command and saving events
   *
   * @param entityId the id of the aggregate to update
   * @param cmd      the command to process
   * @return the updated and persisted aggregate
   */
  public EntityWithIdAndVersion<T> update(String entityId, final CT cmd) {
    return update(entityId, cmd, Optional.empty());
  }

  class LoadedEntityWithMetadata {
    boolean success;
    EntityWithMetadata<T> ewmd;

    LoadedEntityWithMetadata(boolean success, EntityWithMetadata<T> ewmd) {
      this.success = success;
      this.ewmd = ewmd;
    }
  }

  private <T> T withRetry(Supplier<T> asyncRequest) {
    OptimisticLockingException laste = null;
    int MAX_RETRIES = 10;
    for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
      if (laste != null)
        logger.debug("got optimistic locking exception - retrying", laste);
      try {
        return asyncRequest.get();
      } catch (OptimisticLockingException e) {
        laste = e;
      }
    }
    if (logger.isDebugEnabled())
      logger.debug("got exception - NOT retrying: " + MAX_RETRIES, laste);
    throw laste;
  }


  /**
   * Update the specified aggregate by processing a command and saving events
   *
   * @param entityId      the id of the aggregate to update
   * @param cmd           the command to process
   * @param updateOptions options for updating
   * @return the updated and persisted aggregate
   */
  public EntityWithIdAndVersion<T> update(final String entityId, final CT cmd, Optional<UpdateOptions> updateOptions) {
    return updateWithProvidedCommand(entityId, (a) -> Optional.of(cmd), updateOptions);
  }

  /**
   * Update the specified aggregate by processing a command and saving events
   *
   * @param entityId        the id of the aggregate to update
   * @param commandProvider the provider of the command to process
   * @param updateOptions   options for updating
   * @return the updated and persisted aggregate
   */
  public EntityWithIdAndVersion<T> updateWithProvidedCommand(final String entityId, final Function<T, Optional<CT>> commandProvider, Optional<UpdateOptions> updateOptions) {

    return withRetry(() -> {
      final EntityWithMetadata<T> entityWithMetadata;
      try {
        entityWithMetadata = aggregateStore.find(clasz, entityId, updateOptions.map(uo -> new FindOptions().withTriggeringEvent(uo.getTriggeringEvent())));
      } catch (DuplicateTriggeringEventException dtee) {
        return aggregateStore.find(clasz, entityId, Optional.empty()).toEntityWithIdAndVersion();
      }
      final T aggregate = entityWithMetadata.getEntity();

      CommandOutcome commandResult;

      try {
        commandResult = new CommandOutcome(commandProvider.apply(aggregate).map(aggregate::processCommand).orElse(Collections.emptyList()));
      } catch (Throwable e) {
        commandResult = new CommandOutcome(e);
      }

      UpdateEventsAndOptions transformed = transformUpdateEventsAndOptions(updateOptions, aggregate, commandResult);
      List<Event> transformedEvents = transformed.getEvents();
      Optional<UpdateOptions> transformedOptions = transformed.getOptions();

      if (transformedEvents.isEmpty()) {
        return entityWithMetadata.toEntityWithIdAndVersion();
      } else {
        try {
          EntityIdAndVersion entityIdAndVersion = aggregateStore.update(clasz, entityWithMetadata.getEntityIdAndVersion(), transformedEvents,
                  withPossibleSnapshot(transformedOptions, aggregate, entityWithMetadata.getSnapshotVersion(), entityWithMetadata.getEvents(), transformedEvents));
          return new EntityWithIdAndVersion<>(entityIdAndVersion, aggregate);
        } catch (DuplicateTriggeringEventException e) {
          // TODO this should not happen
          EntityWithMetadata<T> reloadedEntity = aggregateStore.find(clasz, entityId, updateOptions.map(uo -> new FindOptions().withTriggeringEvent(uo.getTriggeringEvent())));
          return new EntityWithIdAndVersion<>(reloadedEntity.getEntityIdAndVersion(), aggregate);
        }
      }
    });
  }

  // Duplicate

  private Optional<UpdateOptions> withPossibleSnapshot(Optional<UpdateOptions> updateOptions, T aggregate, Optional<Int128> snapshotVersion, List<EventWithMetadata> oldEvents, List<Event> newEvents) {
    Optional<UpdateOptions> optionsWithSnapshot = aggregateStore.possiblySnapshot(aggregate, snapshotVersion, oldEvents, newEvents)
            .flatMap(snapshot -> Optional.of(updateOptions.orElse(new UpdateOptions()).withSnapshot(snapshot)));
    return optionsWithSnapshot.isPresent() ? optionsWithSnapshot : updateOptions;
  }

  /**
   * Find an aggregate
   *
   * @param entityId the id of the aggregate to find
   * @return the aggregate
   */
  public EntityWithMetadata<T> find(String entityId) {
    return aggregateStore.find(clasz, entityId);
  }

  /**
   * Find an aggregate
   *
   * @param entityId    the id of the aggregate to find
   * @param findOptions options for finding
   * @return the aggregate
   */
  public EntityWithMetadata<T> find(String entityId, FindOptions findOptions) {
    return aggregateStore.find(clasz, entityId, findOptions);
  }

  /**
   * Find an aggregate
   *
   * @param entityId    the id of the aggregate to find
   * @param findOptions options for finding
   * @return the aggregate
   */
  public EntityWithMetadata<T> find(String entityId, Optional<FindOptions> findOptions) {
    return aggregateStore.find(clasz, entityId, findOptions);
  }
}

