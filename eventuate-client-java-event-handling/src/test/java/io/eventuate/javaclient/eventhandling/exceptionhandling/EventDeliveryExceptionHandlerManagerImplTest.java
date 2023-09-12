package io.eventuate.javaclient.eventhandling.exceptionhandling;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EventDeliveryExceptionHandlerManagerImplTest {

  private final Duration retryInterval = Duration.ofSeconds(30);
  private final long retryIntervalMillis = retryInterval.toMillis();
  private final MyException2 throwable =  new MyException2();
  private final int numberOfRetries = 2;

  private EventuateClientScheduler scheduler;
  private EventDeliveryExceptionHandlerManagerImpl manager;
  private Runnable redo;
  private Consumer<Throwable> fail;
  private Runnable ignore;

  @Before
  public void setUp() {
    scheduler = spy(new MyEventuateClientScheduler());


    manager = new EventDeliveryExceptionHandlerManagerImpl(Collections.singletonList(new RetryEventDeliveryExceptionHandler(scheduler)
            .withMaxRetries(numberOfRetries)
            .withRetryInterval(retryInterval)
            .withExceptions(MyException.class, MyException2.class)));
    redo = mock(Runnable.class);
    fail = mock(Consumer.class);
    ignore = mock(Runnable.class);
  }

  @Test
  public void shouldRetryAfterTimeout() {
    EventDeliveryExceptionHandlerWithState rh = manager.getEventHandler(throwable);
    rh.handle(throwable, redo, fail, ignore);
    verify(scheduler).setTimer(eq(retryIntervalMillis), any());
    verify(redo).run();
    verifyNoMoreInteractions(fail);
  }

  @Test
  public void shouldFailForUnsupportedException() {
    Throwable t = new RuntimeException();
    EventDeliveryExceptionHandlerWithState rh = manager.getEventHandler(t);
    rh.handle(t, redo, fail, ignore);

    verify(fail).accept(t);
    verifyNoMoreInteractions(scheduler, redo);
  }



}