package io.eventuate.example.banking.services;


import io.eventuate.AggregateRepository;
import io.eventuate.EntityWithIdAndVersion;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.example.banking.domain.CreateAccountCommand;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class AccountService {
  private final AggregateRepository<Account, AccountCommand> accountRepository;

  public AccountService(AggregateRepository<Account, AccountCommand> accountRepository) {
    this.accountRepository = accountRepository;
  }

  public CompletableFuture<EntityWithIdAndVersion<Account>> openAccount(BigDecimal initialBalance) {
    return accountRepository.save(new CreateAccountCommand(initialBalance));
  }
}
