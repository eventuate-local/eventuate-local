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
public class UpdateInvocation<T extends CommandProcessingAggregate<T, CT>, CT extends Command, C extends CT> implements AggregateOperationInvocation<T,CT, C> {
  private final ArgumentCaptor<C> commandArg;
  private final EntityWithIdAndVersion<T> updatedEntity;

  public UpdateInvocation(ArgumentCaptor<C> commandArg, EntityWithIdAndVersion<T> updatedEntity) {
    this.commandArg = commandArg;
    this.updatedEntity = updatedEntity;
  }

  @Override
  public EntityWithIdAndVersion<T> getEntity() {
    return updatedEntity;
  }

  @Override
  public C getCommand() {
    return commandArg.getValue();
  }
}
