package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogEntryHandler;
import io.eventuate.local.common.BinlogFileOffset;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PostgresWalBinlogEntryHandler extends BinlogEntryHandler {
  public PostgresWalBinlogEntryHandler(EventuateSchema eventuateSchema,
                                       String sourceTableName,
                                       BiConsumer<BinlogEntry, Optional<BinlogFileOffset>> eventConsumer) {

    super(eventuateSchema, sourceTableName, eventConsumer);
  }

  public void accept(BinlogEntry binlogEntry, Optional<BinlogFileOffset> startingBinlogOffset) {
    eventConsumer.accept(binlogEntry, startingBinlogOffset);
  }
}
