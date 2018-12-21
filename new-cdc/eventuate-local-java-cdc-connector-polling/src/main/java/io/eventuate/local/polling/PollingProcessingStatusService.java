package io.eventuate.local.polling;

import io.eventuate.local.common.CdcProcessingStatus;
import io.eventuate.local.common.CdcProcessingStatusService;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

public class PollingProcessingStatusService implements CdcProcessingStatusService {
  private JdbcTemplate jdbcTemplate;
  private String publishedField;
  private Set<String> tables = new HashSet<>();

  public PollingProcessingStatusService(DataSource dataSource, String publishedField) {
    jdbcTemplate = new JdbcTemplate(dataSource);
    this.publishedField = publishedField;
  }

  public void addTable(String table) {
    tables.add(table);
  }

  @Override
  public CdcProcessingStatus getCurrentStatus() {
    return new CdcProcessingStatus(-1, -1, isProcessingFinished());
  }

  @Override
  public void saveEndingOffsetOfLastProcessedEvent(long endingOffsetOfLastProcessedEvent) {
    throw new UnsupportedOperationException();
  }

  private boolean isProcessingFinished() {
    return tables
            .stream()
            .allMatch(table ->
                    jdbcTemplate.queryForObject(String.format("select count(*) from %s where %s = 0 limit 1",
                            table, publishedField), Long.class) == 0);
  }
}