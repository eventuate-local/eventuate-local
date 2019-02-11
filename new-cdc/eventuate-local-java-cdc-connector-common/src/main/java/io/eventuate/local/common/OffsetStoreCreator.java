package io.eventuate.local.common;

import io.eventuate.local.java.common.broker.DataProducer;

public interface OffsetStoreCreator {
  OffsetStore create(DataProducer dataProducer);
}
