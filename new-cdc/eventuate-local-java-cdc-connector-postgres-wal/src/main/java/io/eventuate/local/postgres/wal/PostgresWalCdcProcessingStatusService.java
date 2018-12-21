package io.eventuate.local.postgres.wal;

import io.eventuate.local.common.CdcProcessingStatus;
import io.eventuate.local.common.CdcProcessingStatusService;
import org.postgresql.replication.LogSequenceNumber;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.UUID;

public class PostgresWalCdcProcessingStatusService implements CdcProcessingStatusService {
  private JdbcTemplate jdbcTemplate;
  private volatile long endingOffsetOfLastProcessedEvent;
  private long currentWalPosition;
  private String additionalSlotName;
  private WaitUtil waitUtil;

  public PostgresWalCdcProcessingStatusService(DataSource dataSource,
                                               String additionalSlotName,
                                               long waitForOffsetSyncTimeoutInMilliseconds) {
    jdbcTemplate = new JdbcTemplate(dataSource);
    this.additionalSlotName = additionalSlotName;
    waitUtil = new WaitUtil(waitForOffsetSyncTimeoutInMilliseconds);
  }

  @Override
  public CdcProcessingStatus getCurrentStatus() {
    checkCurrentWalOffsetAndWaitForSyncWithOffsetOfLastProcessedEvent();
    return new CdcProcessingStatus(endingOffsetOfLastProcessedEvent, currentWalPosition);
  }

  @Override
  public void saveEndingOffsetOfLastProcessedEvent(long endingOffsetOfLastProcessedEvent) {
    this.endingOffsetOfLastProcessedEvent = endingOffsetOfLastProcessedEvent;
  }

  private synchronized void checkCurrentWalOffsetAndWaitForSyncWithOffsetOfLastProcessedEvent() {
    if (waitUtil.start()) {
      currentWalPosition = getCurrentWalPosition();
    } else {
      if (currentWalPosition == endingOffsetOfLastProcessedEvent) {
        waitUtil.stop();
      } else {
        waitUtil.tick();
      }
    }
  }

  private long getCurrentWalPosition() {
    try {
      jdbcTemplate.queryForList("SELECT * FROM pg_create_logical_replication_slot(?, 'test_decoding')", additionalSlotName);
      jdbcTemplate.execute("CREATE TABLE if not exists eventuate.replication_test (id VARCHAR(64) PRIMARY KEY)");
      jdbcTemplate.execute("DELETE FROM eventuate.replication_test");
      jdbcTemplate.update("INSERT INTO eventuate.replication_test values (?)", UUID.randomUUID().toString());

      String position = jdbcTemplate.queryForObject("SELECT location FROM pg_logical_slot_get_changes(?, NULL, NULL) ORDER BY location DESC limit 1",
              String.class,
              additionalSlotName);

      return LogSequenceNumber.valueOf(position).asLong();
    } finally {
      jdbcTemplate.queryForList("SELECT * FROM pg_drop_replication_slot(?)", additionalSlotName);
    }
  }
}