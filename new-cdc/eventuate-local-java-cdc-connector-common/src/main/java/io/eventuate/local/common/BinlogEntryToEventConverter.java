package io.eventuate.local.common;

public interface BinlogEntryToEventConverter<EVENT> {
  EVENT convert(BinlogEntry binlogEntry);
}
