package io.eventuate;

import java.util.Optional;

public interface AggregateRepositoryInterceptor<T extends CommandProcessingAggregate<T, CT>, CT extends Command> {

  default UpdateEventsAndOptions transformUpdate(T aggregate, UpdateEventsAndOptions ueo) {
    return ueo;
  }

  default Optional<UpdateEventsAndOptions> handleException(T aggregate, Throwable throwable, Optional<UpdateOptions> updateOptions) {
    return Optional.empty();
  }
}
