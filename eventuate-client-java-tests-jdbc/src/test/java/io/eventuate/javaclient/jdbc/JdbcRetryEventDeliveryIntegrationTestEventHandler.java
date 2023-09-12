package io.eventuate.javaclient.jdbc;


import io.eventuate.EventHandlerContext;
import io.eventuate.EventHandlerMethod;
import io.eventuate.EventSubscriber;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventDeliveryExceptionHandler;
import io.eventuate.testutil.AbstractTestEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.ConcurrentHashMap;

@EventSubscriber(id="eventHandlerRetryEventHandler")
public class JdbcRetryEventDeliveryIntegrationTestEventHandler extends AbstractTestEventHandler {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private ConcurrentHashMap<Integer, Boolean> perSwimLaneFailureToggle = new ConcurrentHashMap<>();

  @Autowired
  private EventDeliveryExceptionHandler eventDeliveryExceptionHandler;

  @EventHandlerMethod
  @Qualifier("forEventHandlerRetryEventHandler")
  public void accountCreated(EventHandlerContext<AccountCreatedEvent> ctx) {
    if (shouldSucceedThisTime(ctx.getSwimlane())) {
      logger.info("throwing RetryEventHandlerException {}", ctx.getSwimlane());
      throw new JdbcRetryEventDeliveryIntegrationTestException();
    } else {
      logger.info("processing {}", ctx.getSwimlane());
      add(ctx);
    }
  }

  private Boolean shouldSucceedThisTime(Integer swimlane) {
    return perSwimLaneFailureToggle.compute(swimlane, (Integer k, Boolean value) -> value == null || !value);
  }

}

