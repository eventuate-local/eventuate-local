package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogEntryToEventConverter;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;

import java.util.Optional;
import java.util.function.Consumer;

public class MySQLCdcProcessor<EVENT extends BinLogEvent> extends DbLogBasedCdcProcessor<EVENT> {

  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  public MySQLCdcProcessor(DbLogClient dbLogClient,
                           OffsetStore offsetStore,
                           BinlogEntryToEventConverter binlogEntryToEventConverter,
                           DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {

    super(dbLogClient, offsetStore, binlogEntryToEventConverter);
    this.debeziumBinlogOffsetKafkaStore = debeziumBinlogOffsetKafkaStore;
  }

  @Override
  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> binlogFileOffset = offsetStore.getLastBinlogFileOffset();

    if (!binlogFileOffset.isPresent()) {
      binlogFileOffset = debeziumBinlogOffsetKafkaStore.getLastBinlogFileOffset();
    }

    Optional<BinlogFileOffset> startingBinlogFileOffset = binlogFileOffset;

    process(eventConsumer, startingBinlogFileOffset);
  }

  public void stop() {
    dbLogClient.stop();
    offsetStore.stop();
  }
}
