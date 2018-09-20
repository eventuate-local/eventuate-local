package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogEntryHandler;

import java.util.function.Consumer;

public class PostgresWalBinlogEntryHandler extends BinlogEntryHandler {
  public PostgresWalBinlogEntryHandler(EventuateSchema eventuateSchema,
                                       String sourceTableName,
                                       Consumer<BinlogEntry> eventConsumer) {

    super(eventuateSchema, sourceTableName, eventConsumer);
  }

  public void accept(BinlogEntry binlogEntry) {
    eventConsumer.accept(binlogEntry);
  }
}
