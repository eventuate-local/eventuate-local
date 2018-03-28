package io.eventuate.local.postgres.wal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostgresWalMessage {
  private PostgresWalChange[] change;

  public PostgresWalMessage() {
  }

  public PostgresWalMessage(PostgresWalChange[] change) {
    this.change = change;
  }

  public PostgresWalChange[] getChange() {
    return change;
  }

  public void setChange(PostgresWalChange[] change) {
    this.change = change;
  }
}