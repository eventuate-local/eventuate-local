package io.eventuate.local.unified.cdc.pipeline.common;

import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.db.log.common.DbLogClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.*;
import java.util.stream.Collectors;

public class BinlogEntryReaderHealthCheck implements HealthIndicator {

  @Value("${eventuatelocal.cdc.max.event.interval.to.assume.reader.healthy:#{60000}}")
  private long maxEventIntervalToAssumeReaderHealthy;

  @Autowired
  private BinlogEntryReaderProvider binlogEntryReaderProvider;

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

    if (!errorMessages.isEmpty()) {
      Health.Builder builder = Health.down();

      for (int i = 1; i <= errorMessages.size(); i++) {
        builder.withDetail("error-" + i, errorMessages.get(i - 1));
      }

      return builder.build();
    }

    return Health.up().build();
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
