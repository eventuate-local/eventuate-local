package io.eventuate.local.db.log.common;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.CdcProcessor;

import java.util.Optional;
import java.util.function.Consumer;

public class DbLogBasedCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  private DbLogClient<EVENT> dbLogClient;
  private DatabaseOffsetKafkaStore databaseOffsetKafkaStore;

  public DbLogBasedCdcProcessor(DbLogClient<EVENT> dbLogClient,
                                DatabaseOffsetKafkaStore databaseOffsetKafkaStore) {

    this.dbLogClient = dbLogClient;
    this.databaseOffsetKafkaStore = databaseOffsetKafkaStore;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> startingBinlogFileOffset = databaseOffsetKafkaStore.getLastBinlogFileOffset();

    try {
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
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    databaseOffsetKafkaStore.stop();
    dbLogClient.stop();
  }
}
