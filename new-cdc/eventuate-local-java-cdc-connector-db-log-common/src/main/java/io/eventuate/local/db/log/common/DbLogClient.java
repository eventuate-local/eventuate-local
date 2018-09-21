package io.eventuate.local.db.log.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.common.BinlogFileOffset;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface DbLogClient extends BinlogEntryReader {
  void addBinlogEntryHandler(EventuateSchema eventuateSchema,
                             String sourceTableName,
                             BiConsumer<BinlogEntry, Optional<BinlogFileOffset>> eventConsumer);
}
