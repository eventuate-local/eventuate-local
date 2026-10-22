package io.eventuate;

public class DefaultAggregateRepositoryInterceptor<T extends CommandProcessingAggregate<T, CT>, CT extends Command> implements AggregateRepositoryInterceptor<T, CT> {
}
