package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.EventContext;
import io.eventuate.javaclient.spring.jdbc.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

public class EventuateLocalJdbcAccess extends EventuateJdbcAccessImpl {

  public EventuateLocalJdbcAccess(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public EventuateLocalJdbcAccess(JdbcTemplate jdbcTemplate, EventuateSchema eventuateSchema) {
    super(jdbcTemplate, eventuateSchema);
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
