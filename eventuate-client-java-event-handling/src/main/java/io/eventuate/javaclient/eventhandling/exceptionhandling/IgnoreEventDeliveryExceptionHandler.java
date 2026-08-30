package io.eventuate.javaclient.eventhandling.exceptionhandling;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * An exception handler that ignores the specified exceptions
 */
public class IgnoreEventDeliveryExceptionHandler implements EventDeliveryExceptionHandler {

  private Class<? extends Throwable>[] supportedThrowables;

  @Override
  public boolean handles(Throwable t) {
    return Arrays.stream(supportedThrowables).anyMatch(th -> th.isInstance(t));
  }

  @Override
  public EventDeliveryExceptionHandlerState makeState(Throwable t) {
    return null;
  }

  @Override
  public void handle(EventDeliveryExceptionHandlerState state, Throwable t, Runnable retry, Consumer<Throwable> fail, Runnable ignore) {
    ignore.run();
  }

  /**
   * Specifies the exceptions that this exception handler can handle
   * @param throwables the exceptions
   * @return this
   */
  @SafeVarargs
  public final IgnoreEventDeliveryExceptionHandler withExceptions(Class<? extends Throwable>... throwables) {
    this.supportedThrowables = throwables;
    return this;
  }

}
