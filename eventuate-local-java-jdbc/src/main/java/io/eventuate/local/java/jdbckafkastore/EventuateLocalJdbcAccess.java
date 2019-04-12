package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.EventContext;
import io.eventuate.javaclient.spring.jdbc.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public class EventuateLocalJdbcAccess extends EventuateJdbcAccessImpl {

  public EventuateLocalJdbcAccess(JdbcTemplate jdbcTemplate, EventuateSqlDialect eventuateSqlDialect) {
    super(jdbcTemplate, eventuateSqlDialect);
  }

  public EventuateLocalJdbcAccess(JdbcTemplate jdbcTemplate, EventuateSchema eventuateSchema, EventuateSqlDialect eventuateSqlDialect) {
    super(jdbcTemplate, eventuateSchema, eventuateSqlDialect);
  }

  @Override
  protected void checkSnapshotForDuplicateEvent(LoadedSnapshot ss, EventContext te) {
    SnapshotTriggeringEvents.checkSnapshotForDuplicateEvent(ss, te);
  }

  @Override
  protected String snapshotTriggeringEvents(Optional<LoadedSnapshot> previousSnapshot, List<EventAndTrigger> events, Optional<EventContext> eventContext) {
    return SnapshotTriggeringEvents.snapshotTriggeringEvents(previousSnapshot, events, eventContext);
  }

}
