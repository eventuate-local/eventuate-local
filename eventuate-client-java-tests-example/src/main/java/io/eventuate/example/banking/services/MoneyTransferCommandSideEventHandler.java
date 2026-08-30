package io.eventuate.example.banking.services;


import io.eventuate.*;
import io.eventuate.example.banking.domain.AccountDebitedEvent;
import io.eventuate.example.banking.domain.MoneyTransferCreatedEvent;
import io.eventuate.example.banking.services.counting.InvocationCounter;
import io.eventuate.testutil.AbstractTestEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventSubscriber(id="javaIntegrationTestCommandSideMoneyTransferEventHandlers",progressNotifications = true)
public class MoneyTransferCommandSideEventHandler extends AbstractTestEventHandler implements Subscriber {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private InvocationCounter invocationCounter;

  public MoneyTransferCommandSideEventHandler(InvocationCounter invocationCounter) {
    this.invocationCounter = invocationCounter;
  }

  @EventHandlerMethod
  public void moneyTransferCreated(EventHandlerContext<MoneyTransferCreatedEvent> ctx) {
    invocationCounter.increment();
    logger.debug("moneyTransferCreated got event {}", ctx.getEventId());
    add(ctx);
  }

  @EventHandlerMethod
  public void doAnything(EventHandlerContext<AccountDebitedEvent> ctx) {
    invocationCounter.increment();
    logger.debug("doAnything got event {} {}", ctx.getEventId(), ctx.getEvent().getTransactionId());
    add(ctx);
  }

  @EventHandlerMethod
  public void noteProgress(EventHandlerContext<EndOfCurrentEventsReachedEvent> ctx) {
    invocationCounter.increment();
    logger.debug("noteProgress got event: " + ctx.getEvent());
    add(ctx);
  }

}

