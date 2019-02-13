package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.CdcProcessingStatus;
import io.eventuate.local.common.CdcProcessingStatusService;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public class MySqlCdcProcessingStatusService implements CdcProcessingStatusService {
  private JdbcTemplate jdbcTemplate;
  private volatile long endingOffsetOfLastProcessedEvent;

  public MySqlCdcProcessingStatusService(DataSource rootDataSource) {
    jdbcTemplate = new JdbcTemplate(rootDataSource);
  }

  @Override
  public CdcProcessingStatus getCurrentStatus() {
    return new CdcProcessingStatus(endingOffsetOfLastProcessedEvent, getCurrentBinlogPosition());
  }

  @Override
  public void saveEndingOffsetOfLastProcessedEvent(long endingOffsetOfLastProcessedEvent) {
    this.endingOffsetOfLastProcessedEvent = endingOffsetOfLastProcessedEvent;
  }

  private long getCurrentBinlogPosition() {
    List<Map<String, Object>> masterStatus = jdbcTemplate.queryForList("SHOW MASTER STATUS");
    Number position = (Number) masterStatus.get(0).get("position");
    return position.longValue();
  }
}