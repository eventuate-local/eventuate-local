package io.eventuate.local.db.log.common;

import io.eventuate.local.common.*;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class DbLogBasedCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  protected DbLogClient dbLogClient;
  protected BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;

  public DbLogBasedCdcProcessor(DbLogClient dbLogClient,
                                BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter) {

    this.dbLogClient = dbLogClient;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
  }

  protected BiConsumer<BinlogEntry, Optional<BinlogFileOffset>> createBinlogConsumer(Consumer<EVENT> eventConsumer) {
    return new BiConsumer<BinlogEntry, Optional<BinlogFileOffset>>() {
      private boolean couldReadDuplicateEntries = true;

      @Override
      public void accept(BinlogEntry binlogEntry, Optional<BinlogFileOffset> startingBinlogFileOffset) {
        if (couldReadDuplicateEntries) {
          if (startingBinlogFileOffset.map(s -> s.isSameOrAfter(binlogEntry.getBinlogFileOffset())).orElse(false)) {
            return;
          } else {
            couldReadDuplicateEntries = false;
          }
        }
        eventConsumer.accept(binlogEntryToEventConverter.convert(binlogEntry));
      }
    };
  }

  @Override
  public void stop() {
  }
}
