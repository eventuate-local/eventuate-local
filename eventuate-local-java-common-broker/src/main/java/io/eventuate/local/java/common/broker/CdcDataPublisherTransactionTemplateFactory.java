package io.eventuate.local.java.common.broker;

public interface CdcDataPublisherTransactionTemplateFactory {
  CdcDataPublisherTransactionTemplate create(DataProducer dataProducer);
}
