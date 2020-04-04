package io.eventuate.javaclient.jdbc;


import io.eventuate.EventHandlerContext;
import io.eventuate.EventHandlerMethod;
import io.eventuate.EventSubscriber;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.example.banking.domain.AccountDebitedEvent;
import io.eventuate.testutil.AbstractTestEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventSubscriber(id="accountMetadataEventHandler")
public class AccountMetadataEventHandler extends AbstractTestEventHandler {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @EventHandlerMethod
  public void accountCreated(EventHandlerContext<AccountCreatedEvent> ctx) {
    add(ctx);
  }

  @EventHandlerMethod
  public void accountDebited(EventHandlerContext<AccountDebitedEvent> ctx) {
    add(ctx);
  }

}

