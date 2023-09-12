package io.eventuate.testutil;

import io.eventuate.Aggregates;
import io.eventuate.Command;
import io.eventuate.DefaultMissingApplyEventMethodStrategy;
import io.eventuate.Event;
import io.eventuate.ReflectiveMutableCommandProcessingAggregate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

import static org.junit.Assert.fail;

/**
 * Abstract base class for writing unit tests for aggregates
 *
 * @param <T>  aggregate type
 * @param <CT> aggregate's command type
 * @see ReflectiveMutableCommandProcessingAggregate
 */
public abstract class AggregateTest<T extends ReflectiveMutableCommandProcessingAggregate<T, CT>, CT extends Command> {

  private Class<T> aggregateClass;
  protected T aggregate;
  protected List<Event> events;
  private CT command;

  protected AggregateTest(Class<T> aggregateClass) {
    this.aggregateClass = aggregateClass;
  }

  /**
   * Process a command that creates an aggregate
   *
   * @param cmd the command to create an aggregate
   * @return the list of events produced by the aggregate
   */
  protected List<Event> create(CT cmd) {
    try {
      this.aggregate = aggregateClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return update(cmd);
  }

  /**
   * Process a cmd that updates an aggregate
   *
   * @param cmd the command to update an aggregate
   * @return the list of events produced by the aggregate
   */
  protected List<Event> update(CT cmd) {
    this.command = cmd;
    events = aggregate.processCommand(cmd);
    Aggregates.applyEventsToMutableAggregate(aggregate, events, DefaultMissingApplyEventMethodStrategy.INSTANCE);
    return events;
  }

  protected void assertEventsEquals(Event... expectedEvents) {
    if (expectedEvents.length != events.size())
      fail(String.format("After processing %s expected %s event(s) but got %s", ToStringBuilder.reflectionToString(command), expectedEvents.length, events.size()));
    for (int i = 0; i < expectedEvents.length; i++) {
      Event expectedEvent = expectedEvents[i];
      if (!EqualsBuilder.reflectionEquals(expectedEvent, events.get(0))) {
        fail(String.format("After processing command %s expected %s th event to be %s but got %s",
                command,
                i,
                ToStringBuilder.reflectionToString(expectedEvent),
                ToStringBuilder.reflectionToString(events.get(0))));
      }
    }
  }

}
