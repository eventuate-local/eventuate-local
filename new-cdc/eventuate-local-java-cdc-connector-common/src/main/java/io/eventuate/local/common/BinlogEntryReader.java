package io.eventuate.local.common;

public interface BinlogEntryReader {
  void start();
  void stop();
}
