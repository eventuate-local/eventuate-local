package io.eventuate.javaclient.domain;

import io.eventuate.CompletableFutureUtil;
import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import io.eventuate.EventContext;
import io.eventuate.common.id.Int128;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventDeliveryExceptionHandlerManager;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventDeliveryExceptionHandlerWithState;
import io.eventuate.javaclient.eventhandling.exceptionhandling.MyException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class EventDispatcherTest {

  private EventDispatcher eventDispatcher;
  private EventDeliveryExceptionHandlerManager exceptionHandlerManager;
  private EventHandler eventHandler;
  private String entityId = null;
  private Int128 eventId = null;
  private MyEvent event = new MyEvent();
  private Integer swimlane = 1;
  private Long offset = 101L;
  private EventContext eventContext = null;
  private Optional<Map<String, String>> eventMetadata = Optional.empty();
  private DispatchedEvent<Event> de = new DispatchedEvent<>(entityId, eventId, event, swimlane, offset, eventContext, eventMetadata);
  private MyException exception = new MyException();

  @Before
  public void setUp() {
    eventHandler = mock(EventHandler.class);
    Map<Class<?>, EventHandler> eventTypesAndHandlers = Collections.singletonMap(MyEvent.class, eventHandler);
    exceptionHandlerManager = mock(EventDeliveryExceptionHandlerManager.class);
    String subscriberId = "sub1";
    eventDispatcher = new EventDispatcher(subscriberId, eventTypesAndHandlers, exceptionHandlerManager);
  }

  @SafeVarargs
  private final void whenEventHandlerDispatchReturning(CompletableFuture<Object>... results) {
    OngoingStubbing<CompletableFuture<?>> stubbing = when(eventHandler.dispatch(de));
    for (CompletableFuture<Object> r : results) {
      stubbing = stubbing.thenAnswer(invocation -> r);
    }
  }

  @Test
  public void shouldDispatchEventSuccessfully() throws InterruptedException, ExecutionException, TimeoutException {

    whenEventHandlerDispatchReturning(CompletableFuture.completedFuture(null));

    eventDispatcher.dispatch(de).get(1, TimeUnit.SECONDS);

    verify(eventHandler).dispatch(de);
    verifyNoMoreInteractions(exceptionHandlerManager);
  }


  @Test
  public void shouldDispatchEventFailAndRetry() throws Exception {

    whenEventHandlerDispatchReturning(CompletableFutureUtil.failedFuture(exception), CompletableFuture.completedFuture(null));

    EventDeliveryExceptionHandlerWithState eventDeliveryExceptionHandlerWithState = mock(EventDeliveryExceptionHandlerWithState.class);

    when(exceptionHandlerManager.getEventHandler(exception)).thenReturn(eventDeliveryExceptionHandlerWithState);

    CompletableFuture<?> result = eventDispatcher.dispatch(de); // won't complete the future

    ArgumentCaptor<Runnable> redoArg = ArgumentCaptor.forClass(Runnable.class);

    verify(eventDeliveryExceptionHandlerWithState).handle(eq(exception), redoArg.capture(), any(Consumer.class), any(Runnable.class));
    verify(exceptionHandlerManager).getEventHandler(exception);

    redoArg.getValue().run();

    result.get(1, TimeUnit.SECONDS);

    verify(eventHandler, times(2)).dispatch(de);

  }
  @Test
  public void shouldDispatchEventFailAndIgnore() throws Exception {

    whenEventHandlerDispatchReturning(CompletableFutureUtil.failedFuture(exception), CompletableFuture.completedFuture(null));

    EventDeliveryExceptionHandlerWithState eventDeliveryExceptionHandlerWithState = mock(EventDeliveryExceptionHandlerWithState.class);

    when(exceptionHandlerManager.getEventHandler(exception)).thenReturn(eventDeliveryExceptionHandlerWithState);

    CompletableFuture<?> result = eventDispatcher.dispatch(de); // won't complete the future

    ArgumentCaptor<Runnable> ignoreArg = ArgumentCaptor.forClass(Runnable.class);

    verify(eventDeliveryExceptionHandlerWithState).handle(eq(exception), any(Runnable.class), any(Consumer.class), ignoreArg.capture());
    verify(exceptionHandlerManager).getEventHandler(exception);

    ignoreArg.getValue().run();

    result.get(1, TimeUnit.SECONDS);

    verify(eventHandler).dispatch(de);

  }

  @Test
  public void shouldDispatchEventFailAndFail() throws Exception {


    whenEventHandlerDispatchReturning(CompletableFutureUtil.failedFuture(exception));

    EventDeliveryExceptionHandlerWithState eventDeliveryExceptionHandlerWithState = mock(EventDeliveryExceptionHandlerWithState.class);

    when(exceptionHandlerManager.getEventHandler(exception)).thenReturn(eventDeliveryExceptionHandlerWithState);

    CompletableFuture<?> result = eventDispatcher.dispatch(de); // won't complete the future

    ArgumentCaptor<Consumer> failArg = ArgumentCaptor.forClass(Consumer.class);

    verify(eventDeliveryExceptionHandlerWithState).handle(eq(exception), any(Runnable.class), failArg.capture(), any(Runnable.class));
    verify(exceptionHandlerManager).getEventHandler(exception);

    failArg.getValue().accept(exception);

    try {
      result.get(1, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      assertEquals(exception, e.getCause());
    }

    verify(eventHandler).dispatch(de);

  }

  class MyEvent implements Event {

  }

}