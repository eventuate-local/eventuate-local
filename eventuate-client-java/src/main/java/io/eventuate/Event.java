package io.eventuate;

/**
 * Base interface for event sourcing events
 *
 * <p>Each aggregate typically defines an interface that is the base interface for all of it's event classes.
 * For example:
 *
 * <pre class="code">
 * &#064;EventEntity(entity="io.eventuate.example.banking.domain.Account")
 *   public interface AccountEvent extends Event {
 *   }
 * </pre>
 *
 * and
 *
 * <pre class="code">
 *  public class AccountDebitedEvent implements AccountEvent {
 *    private BigDecimal amount;
 *    private String transactionId;
 *    ...
 *  }
 * </pre>
 */
public interface Event {
}
