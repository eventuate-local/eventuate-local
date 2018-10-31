package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class CdcMonitoringDao {
  private JdbcTemplate jdbcTemplate;
  private EventuateSchema eventuateSchema;

  public CdcMonitoringDao(DataSource dataSource, EventuateSchema eventuateSchema) {
    jdbcTemplate = new JdbcTemplate(dataSource);
    this.eventuateSchema = eventuateSchema;
  }

  public Optional<Long> selectLastTimeUpdate(long readerId) {
    List<Long> result = jdbcTemplate.queryForList(String.format("select last_time from %s where reader_id = ?",
            eventuateSchema.qualifyTable("cdc_monitoring")), new Object[]{readerId}, Long.class);

    return result.stream().findFirst();
  }

  public void update(long readerId) {

    int rows = jdbcTemplate.update(String.format("update %s set last_time = ? where reader_id = ?",
            eventuateSchema.qualifyTable("cdc_monitoring")), System.currentTimeMillis(), readerId);

    if (rows == 0) {
      jdbcTemplate.update(String.format("insert into %s (reader_id, last_time) values (?, ?)",
              eventuateSchema.qualifyTable("cdc_monitoring")), readerId, System.currentTimeMillis());
    }
  }
}
