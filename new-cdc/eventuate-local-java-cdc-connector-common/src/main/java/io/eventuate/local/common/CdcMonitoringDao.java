package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CdcMonitoringDao {
  private JdbcTemplate jdbcTemplate;
  private EventuateSchema eventuateSchema;
  private ConcurrentHashMap<Long, Long> initializedClients = new ConcurrentHashMap<>();


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
    if (initializedClients.contains(readerId)) {
      jdbcTemplate.update(String.format("update %s set last_time = ? where reader_id = ?",
              eventuateSchema.qualifyTable("cdc_monitoring")), System.currentTimeMillis(), readerId);
    } else {
      try {
        jdbcTemplate.update(String.format("insert into %s (reader_id, last_time) values (?, ?)",
                eventuateSchema.qualifyTable("cdc_monitoring")), readerId, System.currentTimeMillis());
      } catch (DuplicateKeyException e) {
        jdbcTemplate.update(String.format("update %s set last_time = ? where reader_id = ?",
                eventuateSchema.qualifyTable("cdc_monitoring")), System.currentTimeMillis(), readerId);

        initializedClients.put(readerId, readerId);
      }
    }
  }
}
