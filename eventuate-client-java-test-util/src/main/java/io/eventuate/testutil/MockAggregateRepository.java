package io.eventuate.testutil;

import io.eventuate.AggregateRepository;
import io.eventuate.Command;
import io.eventuate.CommandProcessingAggregate;
import io.eventuate.EntityWithIdAndVersion;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

/** A Test helper that simplifies the mocking of AggregateRepositories
 *
 * @param <T> The aggregate type
 * @param <CT> The aggregate's command type
 * @see AggregateRepository
 */
public class MockAggregateRepository<T extends CommandProcessingAggregate<T, CT>, CT extends Command> {

  /**
   * The mock AggregateRepository
   */
  public final AggregateRepository<T, CT> mock = org.mockito.Mockito.mock(AggregateRepository.class);


  /**
   * Create a stub for a call to AggregateRepository.save()
   *
   * @param commandClass The command to expect
   * @param <C> The type of the command
   * @return A SaveInvocation containing the captured command argument and the created entity
   */
  public <C extends CT> SaveInvocation<T, CT, C> whenSave(Class<C> commandClass) {
    EntityWithIdAndVersion<T> createdEntity = new EntityWithIdAndVersion<>(null, null);
    ArgumentCaptor<C> commandArg = ArgumentCaptor.forClass(commandClass);
    when(mock.save(commandArg.capture())).thenReturn(CompletableFuture.completedFuture(createdEntity));
    return new SaveInvocation<>(commandArg, createdEntity);
  }

}
