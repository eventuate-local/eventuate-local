package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.BinlogFileOffset;

import java.util.Optional;

public class DebeziumOffsetStoreMock extends DebeziumBinlogOffsetKafkaStore {

  public DebeziumOffsetStoreMock() {
    super(null, null, null);
  }

  @Override
  public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
    return Optional.empty();
  }
}
