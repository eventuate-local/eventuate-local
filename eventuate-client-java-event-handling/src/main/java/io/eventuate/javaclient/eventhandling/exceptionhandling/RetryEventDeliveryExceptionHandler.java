package io.eventuate.javaclient.eventhandling.exceptionhandling;

import io.eventuate.javaclient.commonimpl.common.EventuateActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * An exception handler that repeatedly retries
 */
public class RetryEventDeliveryExceptionHandler implements EventDeliveryExceptionHandler {

  private final Logger logger = LoggerFactory.getLogger(RetryEventDeliveryExceptionHandler.class);
  private int maxRetries;
  private Class<? extends Throwable>[] supportedThrowables;
  private Duration retryInterval;
  private EventuateClientScheduler eventuateClientScheduler;

  /**
   * Creates a RetryEventDeliveryExceptionHandler that uses the specified scheduler
   * @param eventuateClientScheduler the scheduler
   */
  public RetryEventDeliveryExceptionHandler(EventuateClientScheduler eventuateClientScheduler) {
    this.eventuateClientScheduler = eventuateClientScheduler;
  }

  @Override
  public boolean handles(Throwable t) {
    return Arrays.stream(supportedThrowables).anyMatch(th -> th.isInstance(t));
  }

  @Override
  public EventDeliveryExceptionHandlerState makeState(Throwable t) {
    EventuateActivity.activityLogger.debug("Begin handling: {}", t.getClass().getName());
    return new RetryEventDeliveryExceptionHandlerState(t);
  }

  /**
   * Retry after the timeout unless the max retries has been exceeded or the supplied throwable is not handled
   * @param state the state
   * @param t the throwable
   * @param retry invoked asynchronously to retry
   * @param fail invoked asynchronously to fail
   * @param ignore
   */
  @Override
  public void handle(EventDeliveryExceptionHandlerState state, Throwable t, Runnable retry, Consumer<Throwable> fail, Runnable ignore) {
    RetryEventDeliveryExceptionHandlerState myState = (RetryEventDeliveryExceptionHandlerState) state;
    if (!handles(t) || myState.retries++ >= maxRetries)
      fail.accept(t);
    else {
      long ms = retryInterval.toMillis();
      EventuateActivity.activityLogger.debug("Sleeping for {} ms", ms);
      eventuateClientScheduler.setTimer(ms, () -> {
        EventuateActivity.activityLogger.debug("Retrying delivery");
        retry.run();
      });
    }
  }


  /**
   * Specifies the exceptions that this exception handler can handle
   * @param throwables the exceptions
   * @return this
   */
  @SafeVarargs
  public final RetryEventDeliveryExceptionHandler withExceptions(Class<? extends Throwable>... throwables) {
    this.supportedThrowables = throwables;
    return this;
  }

  /**
   * Specifies the maximum number of retries
   * @param numberOfRetries number of retries to attempt
   * @return this
   */
  public RetryEventDeliveryExceptionHandler withMaxRetries(int numberOfRetries) {
    this.maxRetries = numberOfRetries;
    return this;
  }

  /**
   * Specifies an infinite number of retries
   * @return this
   */
  public RetryEventDeliveryExceptionHandler withInfiniteRetries() {
    return withMaxRetries(Integer.MAX_VALUE);
  }
  /**
   * Specifies the interval between retries
   * @param retryInterval the retry interval
   * @return this
   */
  public RetryEventDeliveryExceptionHandler withRetryInterval(Duration retryInterval) {
    this.retryInterval = retryInterval;
    return this;
  }

}
