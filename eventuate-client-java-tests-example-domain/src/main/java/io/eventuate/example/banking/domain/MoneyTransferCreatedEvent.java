package io.eventuate.example.banking.domain;

public class MoneyTransferCreatedEvent implements MoneyTransferEvent {
  private TransferDetails details;

  public TransferDetails getDetails() {
    return details;
  }

  public MoneyTransferCreatedEvent() {
  }

  public MoneyTransferCreatedEvent(TransferDetails details) {
    this.details = details;
  }
}
