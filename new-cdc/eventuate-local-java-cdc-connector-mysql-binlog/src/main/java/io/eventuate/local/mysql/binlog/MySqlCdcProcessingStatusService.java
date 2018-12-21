package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.CdcProcessingStatus;
import io.eventuate.local.common.CdcProcessingStatusService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public class MySqlCdcProcessingStatusService implements CdcProcessingStatusService {
  private JdbcTemplate jdbcTemplate;
  private volatile long endingOffsetOfLastProcessedEvent;

  public MySqlCdcProcessingStatusService(String dbUrl, String dbUser, String dbPassword) {
    jdbcTemplate = new JdbcTemplate(createDataSource(dbUrl, dbUser, dbPassword));
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

  private DataSource createDataSource(String dbUrl, String dbUser, String dbPassword) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
    dataSource.setUrl(dbUrl);
    dataSource.setUsername(dbUser);
    dataSource.setPassword(dbPassword);
    return dataSource;
  }
}