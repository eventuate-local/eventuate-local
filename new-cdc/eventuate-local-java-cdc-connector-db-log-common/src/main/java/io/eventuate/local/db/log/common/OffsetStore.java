package io.eventuate.local.db.log.common;

import io.eventuate.local.common.BinlogFileOffset;

import java.util.Optional;

public interface OffsetStore {
  Optional<BinlogFileOffset> getLastBinlogFileOffset();
  void save(BinlogFileOffset binlogFileOffset);
  void stop();
}
