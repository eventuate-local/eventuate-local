package io.eventuate.local.polling;

import io.eventuate.local.common.CdcProcessingStatus;
import io.eventuate.local.common.CdcProcessingStatusService;
import io.eventuate.sql.dialect.EventuateSqlDialect;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

public class PollingProcessingStatusService implements CdcProcessingStatusService {
  private JdbcTemplate jdbcTemplate;
  private String publishedField;
  private Set<String> tables = new HashSet<>();
  private EventuateSqlDialect eventuateSqlDialect;

  public PollingProcessingStatusService(DataSource dataSource, String publishedField, EventuateSqlDialect eventuateSqlDialect) {
    jdbcTemplate = new JdbcTemplate(dataSource);
    this.publishedField = publishedField;
    this.eventuateSqlDialect = eventuateSqlDialect;
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
                    jdbcTemplate.queryForObject(eventuateSqlDialect.addLimitToSql(String.format("select count(*) from %s where %s = 0",
                            table, publishedField), "1"), Long.class) == 0);
  }
}