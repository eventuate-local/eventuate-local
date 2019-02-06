package io.eventuate.local.common;

import io.eventuate.local.java.common.broker.DataProducer;

public interface CdcDataPublisherFactory<EVENT extends BinLogEvent> {
  CdcDataPublisher<EVENT> create (DataProducer dataProducer);
}
