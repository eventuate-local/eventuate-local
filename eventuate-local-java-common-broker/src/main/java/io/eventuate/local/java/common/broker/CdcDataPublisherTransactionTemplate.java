package io.eventuate.local.java.common.broker;

public interface CdcDataPublisherTransactionTemplate {
  void inTransaction(Runnable code);
}
