package io.eventuate.local.java.common.broker;

public class EmptyCdcDataPublisherTransactionTemplate implements CdcDataPublisherTransactionTemplate {
  @Override
  public void inTransaction(Runnable code) {

  }
}
