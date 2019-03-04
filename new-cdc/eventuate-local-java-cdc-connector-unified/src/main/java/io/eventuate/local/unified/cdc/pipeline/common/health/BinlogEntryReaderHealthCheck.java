package io.eventuate.local.unified.cdc.pipeline.common.health;

import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import org.springframework.beans.factory.annotation.Value;

public class BinlogEntryReaderHealthCheck extends AbstractHealthCheck {

  @Value("${eventuatelocal.cdc.max.event.interval.to.assume.reader.healthy:#{60000}}")
  private long maxEventIntervalToAssumeReaderHealthy;

  private BinlogEntryReaderProvider binlogEntryReaderProvider;

  public BinlogEntryReaderHealthCheck(BinlogEntryReaderProvider binlogEntryReaderProvider) {
    this.binlogEntryReaderProvider = binlogEntryReaderProvider;
  }

  @Override
  protected void determineHealth(HealthBuilder builder) {

    binlogEntryReaderProvider
            .getAllReaders()
            .forEach(binlogEntryReader -> {

              if (binlogEntryReader.isLeader()) {
                checkBinlogEntryReaderHealth(binlogEntryReader, builder);
                if (binlogEntryReader instanceof DbLogClient) {
                  checkDbLogReaderHealth((DbLogClient) binlogEntryReader, builder);
                }
              } else
                builder.addDetail(String.format("%s is not the leader", binlogEntryReader.getReaderName()));
            });

  }

  private void checkDbLogReaderHealth(DbLogClient dbLogClient, HealthBuilder builder) {
    if (dbLogClient.isConnected()) {
      builder.addDetail(String.format("Reader with id %s is connected",
              dbLogClient.getReaderName()));
    } else {
      builder.addError(String.format("Reader with id %s disconnected",
              dbLogClient.getReaderName()));
    }

  }

  private void checkBinlogEntryReaderHealth(BinlogEntryReader binlogEntryReader, HealthBuilder builder) {
    long age = System.currentTimeMillis() - binlogEntryReader.getLastEventTime();
    boolean eventNotReceivedInTime =
            age > maxEventIntervalToAssumeReaderHealthy;

    if (eventNotReceivedInTime) {
      builder.addError(String.format("Reader with id %s has not received message for %s milliseconds",
              binlogEntryReader.getReaderName(),
              age));
    } else
      builder.addDetail(String.format("Reader with id %s received message %s milliseconds ago",
              binlogEntryReader.getReaderName(),
              age));
  }
}
