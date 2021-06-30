package io.eventuate.example.banking.domain;

import java.math.BigDecimal;

public class TransferDetails {

  private String fromAccountId;
  private String toAccountId;
  private BigDecimal amount;

  private TransferDetails() {
  }

  public TransferDetails(String fromAccountId, String toAccountId, BigDecimal amount) {
    this.fromAccountId = fromAccountId;
    this.toAccountId = toAccountId;
    this.amount = amount;
  }

  public String getFromAccountId() {
    return fromAccountId;
  }

  public String getToAccountId() {
    return toAccountId;
  }

  public BigDecimal getAmount() {
    return amount;
  }
}
