package io.eventuate.local.java.crud;

import io.eventuate.EventContext;
import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.common.jdbc.sqldialect.EventuateSqlDialect;
import io.eventuate.javaclient.jdbc.EventAndTrigger;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccessImpl;
import io.eventuate.javaclient.jdbc.LoadedSnapshot;

import java.util.List;
import java.util.Optional;

public class EventuateLocalJdbcAccess extends EventuateJdbcAccessImpl {

  public EventuateLocalJdbcAccess(IdGenerator idGenerator,
                                  EventuateTransactionTemplate eventuateTransactionTemplate,
                                  EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                  EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                  EventuateSqlDialect eventuateSqlDialect) {
    super(idGenerator, eventuateTransactionTemplate, eventuateJdbcStatementExecutor, eventuateCommonJdbcOperations, eventuateSqlDialect);
  }

  public EventuateLocalJdbcAccess(IdGenerator idGenerator,
                                  EventuateTransactionTemplate eventuateTransactionTemplate,
                                  EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                  EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                  EventuateSqlDialect eventuateSqlDialect,
                                  EventuateSchema eventuateSchema) {
    super(idGenerator, eventuateTransactionTemplate, eventuateJdbcStatementExecutor, eventuateCommonJdbcOperations, eventuateSqlDialect, eventuateSchema);
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
