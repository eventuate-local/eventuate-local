package io.eventuate.local.db.log.common;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;

import java.util.Optional;
import java.util.function.Consumer;

public interface ReplicationLogClient<EVENT extends BinLogEvent> {
  void start(Optional<BinlogFileOffset> binlogFileOffset, Consumer<EVENT> eventConsumer);
  void stop();
}
