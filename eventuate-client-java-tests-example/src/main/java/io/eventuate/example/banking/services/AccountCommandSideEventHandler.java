package io.eventuate.example.banking.services;


import io.eventuate.*;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.example.banking.domain.DebitAccountCommand;
import io.eventuate.example.banking.domain.MoneyTransferCreatedEvent;
import io.eventuate.example.banking.domain.TransferDetails;
import io.eventuate.testutil.AbstractTestEventHandler;

import java.util.concurrent.CompletableFuture;


@EventSubscriber(id="javaIntegrationTestCommandSideAccountEventHandlers")
public class AccountCommandSideEventHandler extends AbstractTestEventHandler implements Subscriber {


  @EventHandlerMethod
  public void doAnything(DispatchedEvent<AccountCreatedEvent> ctx) {
    add(ctx);
  }

  @EventHandlerMethod
  public CompletableFuture<EntityWithIdAndVersion<Account>> debitAccount(EventHandlerContext<MoneyTransferCreatedEvent> ctx) {
    add(ctx);
    logger.debug("debiting account");
    TransferDetails details = ctx.getEvent().getDetails();
    return ctx.update(Account.class, details.getFromAccountId(),
            new DebitAccountCommand(details.getAmount(), ctx.getEntityId()));
  }

}

