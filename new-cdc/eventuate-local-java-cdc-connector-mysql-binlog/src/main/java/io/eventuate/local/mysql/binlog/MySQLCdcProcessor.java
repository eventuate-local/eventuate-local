package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.common.status.StatusService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.function.Consumer;

public class MySQLCdcProcessor<EVENT extends BinLogEvent> extends DbLogBasedCdcProcessor<EVENT> {

  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  @Autowired(required = false)
  private StatusService statusService;

  public MySQLCdcProcessor(DbLogClient<EVENT> dbLogClient,
                           OffsetStore offsetStore,
                           DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {

    super(dbLogClient, offsetStore);
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

    if (statusService != null) {
      statusService.markAsStarted();
    }
  }

  public void stop() {
    dbLogClient.stop();
    offsetStore.stop();
  }
}
