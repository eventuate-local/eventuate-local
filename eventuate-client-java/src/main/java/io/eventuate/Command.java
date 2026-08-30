package io.eventuate;

/**
 * Base interface for event sourcing commands
 *
 * <p>Each aggregate has an interface that extends Command and is the base interface for that aggregate's commands.
 * For example:
 *
 * <pre class="code">
 *   public interface AccountCommand extends Command {
 *   }
 * </pre>
 *
 * and
 *
 * <pre class="code">
 *   public class DebitAccountCommand implements AccountCommand {
 *     private final BigDecimal amount;
 *     private final String transactionId;
 *
 *     public DebitAccountCommand(BigDecimal amount, String transactionId) {
 *
 *     this.amount = amount;
 *     this.transactionId = transactionId;
 *     }
 *
 *     public BigDecimal getAmount() {
 *     return amount;
 *     }
 *
 *     public String getTransactionId() {
 *     return transactionId;
 *     }
 *     }
 * </pre>
 */
public interface Command {
}
