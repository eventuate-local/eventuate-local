package io.eventuate.local.db.log.common;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.CdcProcessor;

import java.util.Optional;
import java.util.function.Consumer;

public class ReplicationLogBasedCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  private ReplicationLogClient<EVENT> postgresWalClient;
  private DatabaseOffsetKafkaStore databaseOffsetKafkaStore;

  public ReplicationLogBasedCdcProcessor(ReplicationLogClient<EVENT> postgresWalClient,
                                         DatabaseOffsetKafkaStore databaseOffsetKafkaStore) {

    this.postgresWalClient = postgresWalClient;
    this.databaseOffsetKafkaStore = databaseOffsetKafkaStore;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> startingBinlogFileOffset = databaseOffsetKafkaStore.getLastBinlogFileOffset();

    try {
      postgresWalClient.start(startingBinlogFileOffset, new Consumer<EVENT>() {
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
    postgresWalClient.stop();
  }
}
