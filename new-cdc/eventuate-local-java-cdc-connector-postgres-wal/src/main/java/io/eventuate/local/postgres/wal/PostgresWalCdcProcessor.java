package io.eventuate.local.postgres.wal;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;

import java.util.Optional;
import java.util.function.Consumer;

public class PostgresWalCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  private DbLogClient<EVENT> dbLogClient;
  private OffsetStore offsetStore;

  public PostgresWalCdcProcessor(DbLogClient<EVENT> dbLogClient,
                                 OffsetStore offsetStore) {

    this.dbLogClient = dbLogClient;
    this.offsetStore = offsetStore;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> startingBinlogFileOffset = offsetStore.getLastBinlogFileOffset();

    dbLogClient.start(startingBinlogFileOffset, new Consumer<EVENT>() {
      private boolean couldReadDuplicateEntries = true;

      @Override
      public void accept(EVENT publishedEvent) {
        if (couldReadDuplicateEntries) {
          if (startingBinlogFileOffset.map(s -> s.isSameOrAfter(publishedEvent.getBinlogFileOffset())).orElse(false)) {
            return;
          } else {
            couldReadDuplicateEntries = false;
          }
        }
        eventConsumer.accept(publishedEvent);
      }
    });
  }

  @Override
  public void stop() {
    offsetStore.stop();
    dbLogClient.stop();
  }
}
