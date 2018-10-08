package io.eventuate.local.java.jdbckafkastore;


import io.eventuate.EventHandlerContext;
import io.eventuate.EventHandlerMethod;
import io.eventuate.EventSubscriber;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.example.banking.domain.AccountDebitedEvent;
import io.eventuate.testutil.AbstractTestEventHandler;

@EventSubscriber(id="accountMetadataEventHandler")
public class AccountMetadataEventHandler extends AbstractTestEventHandler {


  @EventHandlerMethod
  public void accountCreated(EventHandlerContext<AccountCreatedEvent> ctx) {
    add(ctx);
  }

  @EventHandlerMethod
  public void accountDebited(EventHandlerContext<AccountDebitedEvent> ctx) {
    add(ctx);
  }

}

