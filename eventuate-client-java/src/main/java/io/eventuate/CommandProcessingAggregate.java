package io.eventuate;

import java.util.List;

/**
 * An aggregate that processes commands by returning (state changing) events
 * @param <T> the aggregate class
 * @param <CT> the aggregate's command class
 */
public interface CommandProcessingAggregate<T extends CommandProcessingAggregate, CT extends Command>
        extends Aggregate<T> {

  /**
   * Process a command by returning events
   * @param cmd the command to process
   * @return the list of (state changing) events
   */
  List<Event> processCommand(CT cmd);
}
