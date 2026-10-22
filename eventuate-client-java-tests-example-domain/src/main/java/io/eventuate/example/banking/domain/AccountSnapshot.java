package io.eventuate.example.banking.domain;

import io.eventuate.Snapshot;

import java.math.BigDecimal;

public class AccountSnapshot implements Snapshot {
  private BigDecimal balance;

  public AccountSnapshot() {
  }

  public AccountSnapshot(BigDecimal balance) {
    this.balance = balance;
  }

  public BigDecimal getBalance() {
    return balance;
  }
}
