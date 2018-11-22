package io.eventuate.local.unified.cdc.pipeline.common.health;

import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BinlogEntryReaderHealthCheck extends AbstractHealthCheck {

  @Value("${eventuatelocal.cdc.max.event.interval.to.assume.reader.healthy:#{60000}}")
  private long maxEventIntervalToAssumeReaderHealthy;

  private BinlogEntryReaderProvider binlogEntryReaderProvider;

  public BinlogEntryReaderHealthCheck(BinlogEntryReaderProvider binlogEntryReaderProvider) {
    this.binlogEntryReaderProvider = binlogEntryReaderProvider;
  }

  @Override
  public Health health() {

    List<String> errorMessages = binlogEntryReaderProvider
            .getAllReaders()
            .stream()
            .flatMap(binlogEntryReader -> {
              List<String> errors = new ArrayList<>();

              if (binlogEntryReader.isLeader()) {
                errors.addAll(checkBinlogEntryReaderHealth(binlogEntryReader));

                if (binlogEntryReader instanceof DbLogClient) {
                  errors.addAll(checkDbLogReaderHealth((DbLogClient) binlogEntryReader));
                }
              }

              return errors.stream();
            })
            .collect(Collectors.toList());

    return checkErrors(errorMessages);
  }

  private List<String> checkDbLogReaderHealth(DbLogClient dbLogClient) {
    if (dbLogClient.isConnected()) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(String.format("Reader with id %s disconnected",
              dbLogClient.getBinlogClientUniqueId()));
    }
  }

  private List<String> checkBinlogEntryReaderHealth(BinlogEntryReader binlogEntryReader) {
    boolean eventNotReceivedInTime =
            System.currentTimeMillis() - binlogEntryReader.getLastEventTime() > maxEventIntervalToAssumeReaderHealthy;

    if (eventNotReceivedInTime) {
      return Collections.singletonList(String.format("No events received recently by reader %s",
              binlogEntryReader.getBinlogClientUniqueId()));
    }

    return Collections.emptyList();
  }
}
