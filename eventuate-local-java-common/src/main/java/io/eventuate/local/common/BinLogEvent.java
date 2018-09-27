package io.eventuate.local.common;

import java.util.Optional;

public interface BinLogEvent {
  Optional<BinlogFileOffset> getBinlogFileOffset();
}
