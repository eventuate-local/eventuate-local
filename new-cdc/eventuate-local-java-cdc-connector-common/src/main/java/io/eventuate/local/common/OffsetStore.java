package io.eventuate.local.common;

import java.util.Optional;

public interface OffsetStore {
  Optional<BinlogFileOffset> getLastBinlogFileOffset();
  void save(BinlogFileOffset binlogFileOffset);
}
