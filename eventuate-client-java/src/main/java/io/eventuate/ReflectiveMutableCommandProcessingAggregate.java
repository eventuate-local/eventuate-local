package io.eventuate;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * A mutable aggregate that uses reflection to process commands and apply events
 * @param <T> the aggregate class
 * @param <CT> the aggregate's command class
 *
 * @see Event
 * @see Command
 *
 * <p>Here is an example of an aggregate:
 *
 * <pre class="code">
 * public class Account extends ReflectiveMutableCommandProcessingAggregate&gt;Account, AccountCommand&lt; {
 *
 * private BigDecimal balance;
 *
 * public BigDecimal getBalance() {
 *   return balance;
 * }
 * 
 * public List&gt;Event&lt; process(CreateAccountCommand cmd) {
 *   return EventUtil.events(new AccountCreatedEvent(cmd.getInitialBalance()));
 * }
 *
 * public void apply(AccountCreatedEvent event) {
 *   this.balance = event.getInitialBalance();
 * }
 *
 * public List&gt;Event&lt; process(DebitAccountCommand cmd) {
 *   return EventUtil.events(new AccountDebitedEvent(cmd.getAmount(), cmd.getTransactionId()));
 * }
 *
 * public void apply(AccountDebitedEvent event) {
 *   this.balance = this.balance.subtract(event.getAmount());
 * }
 *
 * public List&gt;Event&lt; process(NoopAccountCommand cmd) {
 *  return Collections.emptyList();
 * }
 *
 * }  
 *</pre>
 */
public class ReflectiveMutableCommandProcessingAggregate<T extends ReflectiveMutableCommandProcessingAggregate<T, CT>, CT extends Command>
        implements CommandProcessingAggregate<T, CT> {

  /**
   * Apply an event by invoking an apply() method whose parameter class matches the event's class
   *
   * @param event the event representing the state change
   * @return this
   */
  @Override
  public T applyEvent(Event event) {
    try {
      getClass().getMethod("apply", event.getClass()).invoke(this, event);
    } catch (InvocationTargetException e) {
      throw new EventuateApplyEventFailedUnexpectedlyException(e.getCause());
    } catch (NoSuchMethodException e) {
      throw new MissingApplyMethodException(e, event);
    } catch (IllegalAccessException e) {
      throw new EventuateApplyEventFailedUnexpectedlyException(e);
    }
    return (T)this;
  }

  /**
   * Processes a command by invoking a process() method whose parameter class matches the command's class
   * @param cmd the command to process
   * @return a list (state changing) events
   */
  @Override
  public List<Event> processCommand(CT cmd) {
    try {
      return (List<Event>) getClass().getMethod("process", cmd.getClass()).invoke(this, cmd);
    } catch (InvocationTargetException e) {
      throw new EventuateCommandProcessingFailedException(e.getCause());
    } catch (NoSuchMethodException e) {
      throw new MissingProcessMethodException(e, cmd);
    } catch (IllegalAccessException e) {
      throw new EventuateCommandProcessingFailedUnexpectedlyException(e);
    }
  }
}
