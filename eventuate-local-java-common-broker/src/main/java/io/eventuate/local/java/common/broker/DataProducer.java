package io.eventuate.local.java.common.broker;

import java.util.concurrent.CompletableFuture;

public interface DataProducer {
  CompletableFuture<?> send(String topic, String key, String body);
  void close();
}
