package io.eventuate.local.common;

public interface BinlogEntry {
  Object getColumn(String name);
  BinlogFileOffset getBinlogFileOffset();
}
