package io.eventuate.javaclient.eventhandling.exceptionhandling;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class IgnoreEventDeliveryExceptionHandlerTest {

  private final MyException2 throwable =  new MyException2();
  private IgnoreEventDeliveryExceptionHandler exceptionHandler;
  private Runnable redo;
  private Consumer<Throwable> fail;
  private Runnable ignore;

  @Before
  public void setUp() {

    exceptionHandler = new IgnoreEventDeliveryExceptionHandler()
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
  public void shouldIgnoreException() {
    EventDeliveryExceptionHandlerState state = exceptionHandler.makeState(throwable);
    exceptionHandler.handle(state, throwable, redo, fail, ignore);
    verify(ignore).run();
    verifyNoMoreInteractions(fail, redo);
  }

}