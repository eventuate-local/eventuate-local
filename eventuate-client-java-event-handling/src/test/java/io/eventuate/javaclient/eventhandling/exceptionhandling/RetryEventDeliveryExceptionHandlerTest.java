package io.eventuate.javaclient.eventhandling.exceptionhandling;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RetryEventDeliveryExceptionHandlerTest {

  private final Duration retryInterval = Duration.ofSeconds(30);
  private final long retryIntervalMillis = retryInterval.toMillis();
  private final MyException2 throwable =  new MyException2();
  private final int numberOfRetries = 2;
  private MyEventuateClientScheduler scheduler;
  private RetryEventDeliveryExceptionHandler exceptionHandler;
  private Runnable redo;
  private Consumer<Throwable> fail;
  private Runnable ignore;

  @Before
  public void setUp() {
    scheduler = spy(new MyEventuateClientScheduler());

    exceptionHandler = new RetryEventDeliveryExceptionHandler(scheduler)
            .withMaxRetries(numberOfRetries)
            .withRetryInterval(retryInterval)
            .withExceptions(MyException.class, MyException2.class);
    redo = mock(Runnable.class);
    fail = mock(Consumer.class);
    ignore = mock(Runnable.class);
  }

  @Test
  public void shouldHandle() {
    assertTrue(exceptionHandler.handles(new MyException2()));
    assertFalse(exceptionHandler.handles(new NullPointerException()));
  }

  @Test
  public void shouldRetryException() {
    EventDeliveryExceptionHandlerState state = exceptionHandler.makeState(throwable);
    exceptionHandler.handle(state, throwable, redo, fail, ignore);
    verify(scheduler).setTimer(eq(retryIntervalMillis), any());
    verify(redo).run();
    verifyNoMoreInteractions(fail, ignore);
  }

  @Test
  public void shouldFailAfterRetriesExceeded() {


    EventDeliveryExceptionHandlerState state = exceptionHandler.makeState(throwable);

    IntStream.range(0, numberOfRetries).forEach(i -> exceptionHandler.handle(state, throwable, redo, fail, ignore));

    verify(scheduler, times(numberOfRetries)).setTimer(eq(retryIntervalMillis), any());
    verify(redo, times(numberOfRetries)).run();
    verifyNoMoreInteractions(fail, ignore);

    reset(scheduler, redo, fail);

    exceptionHandler.handle(state, throwable, redo, fail, ignore);
    verify(fail).accept(this.throwable);
    verifyNoMoreInteractions(scheduler, redo, ignore);
  }


  @Test
  public void shouldFailWhenCalledWithUnsupportedException() {
    EventDeliveryExceptionHandlerState state = exceptionHandler.makeState(throwable);
    RuntimeException t = new RuntimeException();
    exceptionHandler.handle(state, t, redo, fail, ignore);
    verify(fail).accept(t);
    verifyNoMoreInteractions(scheduler, redo, ignore);
  }


}