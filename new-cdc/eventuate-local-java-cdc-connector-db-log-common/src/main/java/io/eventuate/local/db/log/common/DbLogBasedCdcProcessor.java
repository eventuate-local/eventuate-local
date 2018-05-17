package io.eventuate.local.db.log.common;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.status.StatusService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.function.Consumer;

public class DbLogBasedCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  protected DbLogClient<EVENT> dbLogClient;
  protected OffsetStore offsetStore;

  @Autowired(required = false)
  private StatusService statusService;

  public DbLogBasedCdcProcessor(DbLogClient<EVENT> dbLogClient,
                                OffsetStore offsetStore) {

    this.dbLogClient = dbLogClient;
    this.offsetStore = offsetStore;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> startingBinlogFileOffset = offsetStore.getLastBinlogFileOffset();

    process(eventConsumer, startingBinlogFileOffset);
  }

  protected void process(Consumer<EVENT> eventConsumer, Optional<BinlogFileOffset> startingBinlogFileOffset) {
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

      if (statusService != null) {
        statusService.markAsStarted();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    offsetStore.stop();
    dbLogClient.stop();
  }
}
