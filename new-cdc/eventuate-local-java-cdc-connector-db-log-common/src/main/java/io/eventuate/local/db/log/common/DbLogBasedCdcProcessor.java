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

  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> startingBinlogFileOffset = offsetStore.getLastBinlogFileOffset();

    process(eventConsumer, startingBinlogFileOffset);
  }

  protected abstract void process(Consumer<EVENT> eventConsumer, Optional<BinlogFileOffset> startingBinlogFileOffset);

  @Override
  public void stop() {
    offsetStore.stop();
    dbLogClient.stop();
  }
}
