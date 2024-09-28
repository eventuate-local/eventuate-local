package io.eventuate.example.banking.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.math.BigDecimal;

public class AccountDebitedEvent implements AccountEvent {
  private BigDecimal amount;
  private String transactionId;

  private AccountDebitedEvent() {
  }

  public AccountDebitedEvent(BigDecimal amount, String transactionId) {
    this.amount = amount;
    this.transactionId = transactionId;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getTransactionId() {
    return transactionId;
  }
}
