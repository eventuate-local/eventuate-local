package io.eventuate.local.db.log.common;

import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogFileOffset;

import java.util.Optional;
import java.util.function.Consumer;

public interface DbLogClient {
  void start(Optional<BinlogFileOffset> binlogFileOffset, Consumer<BinlogEntry> eventConsumer);
  void stop();
}
