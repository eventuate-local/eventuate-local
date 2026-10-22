package io.eventuate.testutil;

import io.eventuate.Command;
import io.eventuate.CommandProcessingAggregate;
import io.eventuate.EntityWithIdAndVersion;
import org.mockito.ArgumentCaptor;

/**
 * Provides access to the captured command and the fake created entity
 *
 * @param <T> The aggregate type
 * @param <CT> The aggregate's command type
 * @param <C> The type of the expected command
 */
public class SaveInvocation<T extends CommandProcessingAggregate<T, CT>, CT extends Command, C extends CT>  implements AggregateOperationInvocation<T,CT, C> {
  private final ArgumentCaptor<C> commandArg;
  private final EntityWithIdAndVersion<T> createdEntity;

  public SaveInvocation(ArgumentCaptor<C> commandArg, EntityWithIdAndVersion<T> createdEntity) {
    this.commandArg = commandArg;
    this.createdEntity = createdEntity;
  }

  public EntityWithIdAndVersion<T> getEntity() {
    return createdEntity;
  }

  public C getCommand() {
    return commandArg.getValue();
  }
}
