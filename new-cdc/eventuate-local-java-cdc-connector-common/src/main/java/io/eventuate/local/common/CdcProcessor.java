package io.eventuate.local.common;

import java.util.function.Consumer;

public interface CdcProcessor<EVENT> {
  void start(Consumer<EVENT> eventConsumer);
  void stop();
}
