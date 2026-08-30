package io.eventuate;

/**
 * Base interface for an Aggregate that uses event sourcing
 * @param <T> the aggregate class
 */
public interface Aggregate<T extends Aggregate> {

  /**
   * Update the aggregate
   * @param event the event representing the state change
   * @return the updated aggregate, which might be this
   */
  T applyEvent(Event event);
}