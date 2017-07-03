package io.eventuate.local.common;

public interface BinLogEvent {
  BinlogFileOffset getBinlogFileOffset();
}
