package io.eventuate.javaclient.domain;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class SwimlaneBasedDispatcher {

  private static Logger logger = LoggerFactory.getLogger(SwimlaneBasedDispatcher.class);

  private final ConcurrentHashMap<Integer, SwimlaneDispatcher> map = new ConcurrentHashMap<>();
  private Executor executor;
  private String subscriberId;

  public SwimlaneBasedDispatcher(String subscriberId, Executor executor) {
    this.subscriberId = subscriberId;
    this.executor = executor;
  }

  public CompletableFuture<?> dispatch(DispatchedEvent<Event> de, Function<DispatchedEvent<Event>, CompletableFuture<?>> target) {
    Integer swimlane = de.getSwimlane();
    SwimlaneDispatcher stuff = map.get(swimlane);
    if (stuff == null) {
      logger.trace("No dispatcher for {} {}. Attempting to create", subscriberId, swimlane);
      stuff = new SwimlaneDispatcher(subscriberId, swimlane, executor);
      SwimlaneDispatcher r = map.putIfAbsent(swimlane, stuff);
      if (r != null) {
        logger.trace("Using concurrently created SwimlaneDispatcher for {} {}", subscriberId, swimlane);
        stuff = r;
      } else {
        logger.trace("Using newly created SwimlaneDispatcher for {} {}", subscriberId, swimlane);
      }
    }
    return stuff.dispatch(de, target);
  }
}

