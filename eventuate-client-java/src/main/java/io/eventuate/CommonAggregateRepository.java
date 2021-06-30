package io.eventuate;

import java.util.List;
import java.util.Optional;

public abstract  class CommonAggregateRepository<T extends CommandProcessingAggregate<T, CT>, CT extends Command> {

  protected Class<T> clasz;

  protected AggregateRepositoryInterceptor<T, CT > interceptor = new DefaultAggregateRepositoryInterceptor<>();

  protected MissingApplyEventMethodStrategy missingApplyEventMethodStrategy = new DefaultMissingApplyEventMethodStrategy();

  public CommonAggregateRepository(Class<T> clasz) {
    this.clasz = clasz;
  }

  public void setMissingApplyEventMethodStrategy(MissingApplyEventMethodStrategy missingApplyEventMethodStrategy) {
    this.missingApplyEventMethodStrategy = missingApplyEventMethodStrategy;
  }

  public void setInterceptor(AggregateRepositoryInterceptor<T, CT> interceptor) {
    this.interceptor = interceptor;
  }

  protected UpdateEventsAndOptions transformUpdateEventsAndOptions(Optional<UpdateOptions> updateOptions, T aggregate, CommandOutcome commandResult) {
    UpdateEventsAndOptions transformed;
    if (commandResult.isFailure()) {
      Optional<UpdateEventsAndOptions> handled = effectiveInterceptor(updateOptions).handleException(aggregate, commandResult.throwable, updateOptions);
      if (handled.isPresent())
        transformed = handled.get();
      else {
        throw new EventuateCommandProcessingFailedException(commandResult.throwable);
      }
    } else {
      List<Event> events = commandResult.result;

      Aggregates.applyEventsToMutableAggregate(aggregate, events, missingApplyEventMethodStrategy);
      UpdateEventsAndOptions original = new UpdateEventsAndOptions(events, updateOptions);
      transformed = effectiveInterceptor(updateOptions).transformUpdate(aggregate, original);
    }
    return transformed;
  }

  private AggregateRepositoryInterceptor effectiveInterceptor(Optional<UpdateOptions> updateOptions) {
    return updateOptions.flatMap(uo -> uo.getInterceptor()).orElse(interceptor);
  }
}
