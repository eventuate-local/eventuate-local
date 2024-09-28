package io.eventuate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used on an Event class or interface to specify the aggregate that publishes the event
 *
 * <pre class="code">
 *
 *   &#064;EventEntity(entity="io.eventuate.example.banking.domain.Account")
 *   public interface AccountEvent extends Event {
 *   }
 *</pre>
 * @see Event
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EventEntity {

  /**
   * The aggregate class
   * @return The fully qualified class name of the event
   */
  String entity();
}