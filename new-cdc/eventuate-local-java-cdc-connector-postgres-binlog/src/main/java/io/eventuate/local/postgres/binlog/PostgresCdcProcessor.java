package io.eventuate.local.postgres.binlog;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.CdcProcessor;

import java.util.Optional;
import java.util.function.Consumer;

public class PostgresCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  private PostgresBinaryLogClient<EVENT> postgresBinaryLogClient;
  private DatabaseLastSequenceNumberKafkaStore databaseLastSequenceNumberKafkaStore;

  public PostgresCdcProcessor(PostgresBinaryLogClient<EVENT> postgresBinaryLogClient,
          DatabaseLastSequenceNumberKafkaStore databaseLastSequenceNumberKafkaStore) {

    this.postgresBinaryLogClient = postgresBinaryLogClient;
    this.databaseLastSequenceNumberKafkaStore = databaseLastSequenceNumberKafkaStore;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> startingBinlogFileOffset = databaseLastSequenceNumberKafkaStore.getLastBinlogFileOffset();

    try {
      postgresBinaryLogClient.start(startingBinlogFileOffset, new Consumer<EVENT>() {
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

  }
}
