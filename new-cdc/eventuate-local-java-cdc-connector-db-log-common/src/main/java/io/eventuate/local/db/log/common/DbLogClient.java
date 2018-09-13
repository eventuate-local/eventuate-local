package io.eventuate.local.db.log.common;

import io.eventuate.local.common.BinlogFileOffset;

import java.util.Optional;

public interface DbLogClient {
  void start(Optional<BinlogFileOffset> binlogFileOffset);
  void stop();
}
