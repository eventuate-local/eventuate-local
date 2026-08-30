package io.eventuate.example.banking.domain;

import java.math.BigDecimal;

public class CreateAccountCommand implements AccountCommand {
  private BigDecimal initialBalance;

  public CreateAccountCommand(BigDecimal initialBalance) {
    this.initialBalance = initialBalance;
  }

  public BigDecimal getInitialBalance() {
    return initialBalance;
  }

  public void setInitialBalance(BigDecimal initialBalance) {
    this.initialBalance = initialBalance;
  }
}
