package io.eventuate.example.banking.domain;


import io.eventuate.Event;
import io.eventuate.EventUtil;
import io.eventuate.ReflectiveMutableCommandProcessingAggregate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class Account extends ReflectiveMutableCommandProcessingAggregate<Account, AccountCommand> {

  private BigDecimal balance;

  public BigDecimal getBalance() {
    return balance;
  }

  public List<Event> process(CreateAccountCommand cmd) {
    return EventUtil.events(new AccountCreatedEvent(cmd.getInitialBalance()));
  }

  public void apply(AccountCreatedEvent event) {
    this.balance = event.getInitialBalance();
  }

  public List<Event> process(DebitAccountCommand cmd) {
    return EventUtil.events(new AccountDebitedEvent(cmd.getAmount(), cmd.getTransactionId()));
  }

  public void apply(AccountDebitedEvent event) {
    this.balance = this.balance.subtract(event.getAmount());
  }

  public List<Event> process(NoopAccountCommand cmd) {
    return Collections.emptyList();
  }

}
