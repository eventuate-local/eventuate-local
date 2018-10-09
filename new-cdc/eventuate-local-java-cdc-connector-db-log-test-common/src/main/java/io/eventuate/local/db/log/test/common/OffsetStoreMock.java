package io.eventuate.local.db.log.test.common;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.db.log.common.OffsetStore;

import java.util.Optional;

public class OffsetStoreMock implements OffsetStore {

  Optional<BinlogFileOffset> binlogFileOffset = Optional.empty();

  @Override
  public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
    return binlogFileOffset;
  }

  @Override
  public void save(BinlogFileOffset binlogFileOffset) {
    this.binlogFileOffset = Optional.ofNullable(binlogFileOffset);
  }

  @Override
  public void stop() {
  }
}
