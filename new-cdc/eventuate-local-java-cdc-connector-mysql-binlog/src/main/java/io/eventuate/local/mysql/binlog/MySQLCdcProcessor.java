package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.DbLogClient;

import java.util.Optional;
import java.util.function.Consumer;

public class MySQLCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  private DbLogClient<EVENT> dbLogClient;
  private DatabaseOffsetKafkaStore databaseOffsetKafkaStore;
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  public MySQLCdcProcessor(DbLogClient<EVENT> dbLogClient,
                           DatabaseOffsetKafkaStore databaseOffsetKafkaStore,
                           DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {

    this.dbLogClient = dbLogClient;
    this.databaseOffsetKafkaStore = databaseOffsetKafkaStore;
    this.debeziumBinlogOffsetKafkaStore = debeziumBinlogOffsetKafkaStore;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> binlogFileOffset = databaseOffsetKafkaStore.getLastBinlogFileOffset();

    if (!binlogFileOffset.isPresent()) {
      binlogFileOffset = debeziumBinlogOffsetKafkaStore.getLastBinlogFileOffset();
    }

    Optional<BinlogFileOffset> startingBinlogFileOffset = binlogFileOffset;

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

  public void stop() {
    dbLogClient.stop();
    databaseOffsetKafkaStore.stop();
  }
}
