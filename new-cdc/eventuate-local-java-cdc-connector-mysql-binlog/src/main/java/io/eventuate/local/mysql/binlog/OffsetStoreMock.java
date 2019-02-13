package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.OffsetStore;

import java.util.Optional;

public class OffsetStoreMock implements OffsetStore {

  Optional<BinlogFileOffset> binlogFileOffset = Optional.empty();

  @Override
  public synchronized Optional<BinlogFileOffset> getLastBinlogFileOffset() {
    return binlogFileOffset;
  }

  @Override
  public synchronized void save(BinlogFileOffset binlogFileOffset) {
    this.binlogFileOffset = Optional.ofNullable(binlogFileOffset);
  }
}
