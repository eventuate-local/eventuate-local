package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;

import java.util.Optional;

public class PostgresWalBinlogEntryHandler<EVENT extends BinLogEvent> extends BinlogEntryHandler<EVENT> {
  private boolean couldReadDuplicateEntries = true;

  public PostgresWalBinlogEntryHandler(EventuateSchema eventuateSchema,
                                       String sourceTableName,
                                       BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                       CdcDataPublisher<EVENT> dataPublisher) {

    super(eventuateSchema, sourceTableName, binlogEntryToEventConverter, dataPublisher);
  }

  public void accept(BinlogEntry binlogEntry, Optional<BinlogFileOffset> startingBinlogOffset) {
    if (couldReadDuplicateEntries) {
      if (startingBinlogOffset.map(s -> s.isSameOrAfter(binlogEntry.getBinlogFileOffset())).orElse(false)) {
        return;
      } else {
        couldReadDuplicateEntries = false;
      }
    }

    cdcDataPublisher.handleEvent(binlogEntryToEventConverter.convert(binlogEntry));
  }
}
