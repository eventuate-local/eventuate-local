package io.eventuate.example.banking.services;

import io.eventuate.DispatchedEvent;
import io.eventuate.EventHandlerMethod;
import io.eventuate.EventSubscriber;
import io.eventuate.Subscriber;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.testutil.AbstractTestEventHandler;

@EventSubscriber(id="javaIntegrationTestQuerySideAccountEventHandlers")
public class AccountQuerySideEventHandler extends AbstractTestEventHandler implements Subscriber {

  @EventHandlerMethod
  public void create(DispatchedEvent<AccountCreatedEvent> de) {
    add(de);
  }
}
