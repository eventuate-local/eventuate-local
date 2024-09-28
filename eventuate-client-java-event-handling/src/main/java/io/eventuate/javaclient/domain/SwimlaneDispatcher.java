package io.eventuate.javaclient.domain;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class SwimlaneDispatcher {

  private static Logger logger = LoggerFactory.getLogger(SwimlaneDispatcher.class);

  private String subscriberId;
  private Integer swimlane;
  private Executor executor;

  private final LinkedBlockingQueue<QueuedEvent> queue = new LinkedBlockingQueue<>();
  private AtomicBoolean running = new AtomicBoolean(false);

  public SwimlaneDispatcher(String subscriberId, Integer swimlane, Executor executor) {
    this.subscriberId = subscriberId;
    this.swimlane = swimlane;
    this.executor = executor;
  }

  public CompletableFuture<?> dispatch(DispatchedEvent<Event> de, Function<DispatchedEvent<Event>, CompletableFuture<?>> target) {
    synchronized (queue) {
      QueuedEvent qe = new QueuedEvent(de, target);
      queue.add(qe);
      logger.trace("added event to queue: {} {} {}", subscriberId, swimlane, de);
      if (running.compareAndSet(false, true)) {
        logger.trace("Stopped - attempting to process newly queued event: {} {}", subscriberId, swimlane);
        processNextQueuedEvent();
      } else
        logger.trace("Running - Not attempting to process newly queued event: {} {}", subscriberId, swimlane);
      return qe.future;
    }
  }

  private void processNextQueuedEvent() {
    executor.execute(this::processQueuedEvent);
  }

  class QueuedEvent {
    DispatchedEvent<Event> event;
    private Function<DispatchedEvent<Event>, CompletableFuture<?>> target;
    CompletableFuture<Object> future = new CompletableFuture<>();

    public QueuedEvent(DispatchedEvent<Event> event, Function<DispatchedEvent<Event>, CompletableFuture<?>> target) {
      this.event = event;
      this.target = target;
    }
  }


  public void processQueuedEvent() {
    QueuedEvent qe = getNextEvent();
    if (qe == null)
      logger.trace("No queued event for {} {}", subscriberId, swimlane);
    else {
      logger.trace("Invoking handler for event for {} {} {}", subscriberId, swimlane, qe.event);
      qe.target.apply(qe.event).handle((success, throwable) -> {
        if (throwable == null) {
          logger.debug("Handler succeeded for event for {} {} {}", subscriberId, swimlane, qe.event);
          boolean x = qe.future.complete(success);
          logger.trace("Completed future success {}", x);
          logger.trace("Maybe processing next queued event {} {}", subscriberId, swimlane);
          processNextQueuedEvent();
        } else {
          logger.error(String.format("handler for %s %s  %s failed: ", subscriberId, swimlane, qe.event), throwable);
          boolean x = qe.future.completeExceptionally(throwable);
          logger.trace("Completed future failed{}", x);
          // TODO - what to do here???
        }
        return null;
      });
    }
  }

  private QueuedEvent getNextEvent() {
    QueuedEvent qe1 = queue.poll();
    if (qe1 != null)
      return qe1;

    synchronized (queue) {
      QueuedEvent qe = queue.poll();
      if (qe == null) {
        running.compareAndSet(true, false);
      }
      return qe;
    }
  }

}
