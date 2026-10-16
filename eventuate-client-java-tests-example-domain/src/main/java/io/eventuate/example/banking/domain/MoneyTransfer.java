package io.eventuate.example.banking.domain;


import io.eventuate.Event;
import io.eventuate.EventUtil;
import io.eventuate.ReflectiveMutableCommandProcessingAggregate;

import java.util.List;

public class MoneyTransfer extends ReflectiveMutableCommandProcessingAggregate<MoneyTransfer, AccountCommand> {

  public List<Event> process(CreateMoneyTransferCommand cmd) {
    return EventUtil.events(new MoneyTransferCreatedEvent(cmd.getDetails()));
  }

  public void apply(AccountCreatedEvent event) {
    // TODO - do something
  }
}
