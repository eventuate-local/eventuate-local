package io.eventuate.common.jdbckafkastore;

import io.eventuate.EventContext;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.javaclient.jdbc.EventAndTrigger;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccessImpl;
import io.eventuate.javaclient.jdbc.LoadedSnapshot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

public class EventuateLocalJdbcAccess extends EventuateJdbcAccessImpl {

  public EventuateLocalJdbcAccess(TransactionTemplate transactionTemplate,
                                  JdbcTemplate jdbcTemplate,
                                  EventuateCommonJdbcOperations eventuateCommonJdbcOperations) {
    super(transactionTemplate, jdbcTemplate, eventuateCommonJdbcOperations);
  }

  public EventuateLocalJdbcAccess(TransactionTemplate transactionTemplate,
                                  JdbcTemplate jdbcTemplate,
                                  EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                  EventuateSchema eventuateSchema) {
    super(transactionTemplate, jdbcTemplate, eventuateCommonJdbcOperations, eventuateSchema);
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
