package io.eventuate;

import java.util.List;

public class Aggregates {
  public static <T extends Aggregate<T>> T applyEventsToMutableAggregate(T aggregate, List<Event> events, MissingApplyEventMethodStrategy missingApplyEventMethodStrategy) {
    for (Event event : events) {
      try {
        aggregate = aggregate.applyEvent(event);
      } catch (MissingApplyMethodException e) {
        missingApplyEventMethodStrategy.handle(aggregate, e);
      }
    }
    return aggregate;
  }

  public static <T extends Aggregate<T>> T recreateAggregate(Class<T> clasz, List<Event> events, MissingApplyEventMethodStrategy missingApplyEventMethodStrategy) {
    return applyEventsToMutableAggregate(newAggregate(clasz), events, missingApplyEventMethodStrategy);
  }

  private static <T extends Aggregate<T>> T newAggregate(Class<T> clasz) {
    try {
      return clasz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
