package io.eventuate.local.db.log.common;

import io.eventuate.local.common.*;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class DbLogBasedCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  protected DbLogClient dbLogClient;
  protected OffsetStore offsetStore;
  protected BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;

  public DbLogBasedCdcProcessor(DbLogClient dbLogClient,
                                OffsetStore offsetStore,
                                BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter) {

    this.dbLogClient = dbLogClient;
    this.offsetStore = offsetStore;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
  }

  protected Consumer<BinlogEntry> createBinlogConsumer(Consumer<EVENT> eventConsumer, Optional<BinlogFileOffset> startingBinlogFileOffset) {
    return new Consumer<BinlogEntry>() {
      private boolean couldReadDuplicateEntries = true;

      @Override
      public void accept(BinlogEntry binlogEntry) {
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
    offsetStore.stop();
    dbLogClient.stop();
  }
}
