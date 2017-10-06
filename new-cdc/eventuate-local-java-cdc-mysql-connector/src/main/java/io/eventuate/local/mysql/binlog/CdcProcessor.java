package io.eventuate.local.mysql.binlog;

import java.util.function.Consumer;

public interface CdcProcessor<EVENT> {
  void start(Consumer<EVENT> eventConsumer);
  void stop();
}
