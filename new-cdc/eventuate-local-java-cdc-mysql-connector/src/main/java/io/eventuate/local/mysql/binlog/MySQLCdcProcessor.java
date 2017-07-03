package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.PublishedEvent;

import java.util.Optional;
import java.util.function.Consumer;

public class MySQLCdcProcessor<M extends BinLogEvent> {

  private MySqlBinaryLogClient<M> mySqlBinaryLogClient;
  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;

  public MySQLCdcProcessor(MySqlBinaryLogClient<M> mySqlBinaryLogClient, DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore) {
    this.mySqlBinaryLogClient = mySqlBinaryLogClient;
    this.binlogOffsetKafkaStore = binlogOffsetKafkaStore;
  }

  public void start(Consumer<M> eventConsumer) {
    Optional<BinlogFileOffset> startingbinlogFileOffset = binlogOffsetKafkaStore.getLastBinlogFileOffset();
    try {
      mySqlBinaryLogClient.start(startingbinlogFileOffset, new Consumer<M>() {
        private boolean couldReadDuplicateEntries = true;

        @Override
        public void accept(M publishedEvent) {
          if (couldReadDuplicateEntries) {
            if (startingbinlogFileOffset.map(s -> s.isSameOrAfter(publishedEvent.getBinlogFileOffset())).orElse(false)) {
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

  public void stop() {
    mySqlBinaryLogClient.stop();
    binlogOffsetKafkaStore.stop();
  }
}
