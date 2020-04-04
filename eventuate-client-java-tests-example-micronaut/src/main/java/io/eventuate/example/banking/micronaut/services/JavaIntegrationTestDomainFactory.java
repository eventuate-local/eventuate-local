package io.eventuate.example.banking.micronaut.services;

import io.eventuate.AggregateRepository;
import io.eventuate.EventuateAggregateStore;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.example.banking.services.AccountCommandSideEventHandler;
import io.eventuate.example.banking.services.AccountQuerySideEventHandler;
import io.eventuate.example.banking.services.AccountService;
import io.eventuate.example.banking.services.MoneyTransferCommandSideEventHandler;
import io.eventuate.example.banking.services.counting.InvocationCounter;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class JavaIntegrationTestDomainFactory {

  @Singleton
  public AccountCommandSideEventHandler accountCommandSideEventHandler() {
    return new AccountCommandSideEventHandler();
  }

  @Singleton
  public MoneyTransferCommandSideEventHandler moneyTransferCommandSideEventHandler(InvocationCounter invocationCounter) {
    return new MoneyTransferCommandSideEventHandler(invocationCounter);
  }

  @Singleton
  public AccountQuerySideEventHandler accountQuerySideEventHandler() {
    return new AccountQuerySideEventHandler();
  }

  @Singleton
  public AccountService accountService(AggregateRepository<Account, AccountCommand> accountRepository) {
    return new AccountService(accountRepository);
  }

  @Singleton
  public AggregateRepository<Account, AccountCommand> accountRepository(EventuateAggregateStore aggregateStore) {
    return new AggregateRepository<>(Account.class, aggregateStore);
  }

  @Singleton
  public InvocationCounter invocationCounter() {
    return new InvocationCounter();
  }
}
