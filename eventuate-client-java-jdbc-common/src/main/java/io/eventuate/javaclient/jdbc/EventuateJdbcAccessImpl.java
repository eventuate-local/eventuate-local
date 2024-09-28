package io.eventuate.javaclient.jdbc;

import io.eventuate.Aggregate;
import io.eventuate.DuplicateTriggeringEventException;
import io.eventuate.EntityAlreadyExistsException;
import io.eventuate.EntityIdAndType;
import io.eventuate.EntityNotFoundException;
import io.eventuate.EventContext;
import io.eventuate.OptimisticLockingException;
import io.eventuate.common.id.ApplicationIdGenerator;
import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.id.Int128;
import io.eventuate.common.jdbc.*;
import io.eventuate.common.jdbc.sqldialect.EventuateSqlDialect;
import io.eventuate.javaclient.commonimpl.common.EventIdTypeAndData;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.crud.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventuateJdbcAccessImpl implements EventuateJdbcAccess {
  protected Logger logger = LoggerFactory.getLogger(getClass());

  private IdGenerator idGenerator;
  private ApplicationIdGenerator applicationIdGenerator = new ApplicationIdGenerator();

  private EventuateTransactionTemplate eventuateTransactionTemplate;
  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;
  private String entityTable;
  private String eventTable;
  private String snapshotTable;

  private EventuateCommonJdbcOperations eventuateCommonJdbcOperations;
  private EventuateSqlDialect eventuateSqlDialect;
  private EventuateSchema eventuateSchema;

  public EventuateJdbcAccessImpl(IdGenerator idGenerator,
                                 EventuateTransactionTemplate eventuateTransactionTemplate,
                                 EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                 EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                 EventuateSqlDialect eventuateSqlDialect) {

    this(idGenerator, eventuateTransactionTemplate, eventuateJdbcStatementExecutor, eventuateCommonJdbcOperations, eventuateSqlDialect, new EventuateSchema());
  }

  public EventuateJdbcAccessImpl(IdGenerator idGenerator,
                                 EventuateTransactionTemplate eventuateTransactionTemplate,
                                 EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                 EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                 EventuateSqlDialect eventuateSqlDialect,
                                 EventuateSchema eventuateSchema) {

    this.idGenerator = idGenerator;
    this.eventuateTransactionTemplate = eventuateTransactionTemplate;
    this.eventuateJdbcStatementExecutor = eventuateJdbcStatementExecutor;
    this.eventuateCommonJdbcOperations = eventuateCommonJdbcOperations;
    this.eventuateSqlDialect = eventuateSqlDialect;
    this.eventuateSchema = eventuateSchema;

    entityTable = eventuateSchema.qualifyTable("entities");
    eventTable = eventuateSchema.qualifyTable("events");
    snapshotTable = eventuateSchema.qualifyTable("snapshots");
  }

  @Override
  public SaveUpdateResult save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> saveOptions) {
    return eventuateTransactionTemplate.executeInTransaction(() -> saveWithoutTransaction(aggregateType, events, saveOptions));
  }

  private SaveUpdateResult saveWithoutTransaction(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> saveOptions) {
    String entityId = saveOptions
            .flatMap(AggregateCrudSaveOptions::getEntityId)
            .orElse(applicationIdGenerator.genIdAsString());

    List<EventIdTypeAndData> eventsWithIds = new ArrayList<>();

    //Inserting events before entity to use last event id as entity version.
    // In case if entity already exists there will be transaction rollback (executes only in transaction)
    for (EventTypeAndData event : events) {
      String eventId = eventuateCommonJdbcOperations.insertIntoEventsTable(idGenerator,
              entityId,
              event.getEventData(),
              event.getEventType(),
              aggregateType,
              saveOptions.flatMap(AggregateCrudSaveOptions::getTriggeringEvent).map(EventContext::getEventToken),
              event.getMetadata(),
              eventuateSchema);

      eventsWithIds.add(new EventIdTypeAndData(Int128.fromString(eventId), event.getEventType(), event.getEventData(), event.getMetadata()));
    }

    Int128 entityVersion = last(eventsWithIds).getId();

    try {
      eventuateJdbcStatementExecutor.update(String.format("INSERT INTO %s (entity_type, entity_id, entity_version) VALUES (?, ?, ?)", entityTable),
              aggregateType, entityId, entityVersion.asString());
    } catch (EventuateDuplicateKeyException e) {
      throw new EntityAlreadyExistsException();
    }

    List<Int128> eventIds = eventsWithIds.stream().map(EventIdTypeAndData::getId).collect(Collectors.toList());

    return new SaveUpdateResult(new EntityIdVersionAndEventIds(entityId, entityVersion, eventIds),
            new PublishableEvents(aggregateType, entityId, eventsWithIds));
  }


  private <T> T last(List<T> eventsWithIds) {
    return eventsWithIds.get(eventsWithIds.size() - 1);
  }

  private final EventuateRowMapper<EventAndTrigger> eventAndTriggerRowMapper = (rs, rowNum) -> {
    String eventId;

    if (idGenerator.databaseIdRequired()) {
      eventId = idGenerator.genIdAsString(rs.getLong("id"), null);
    } else {
      eventId = rs.getString("event_id");
    }

    String eventType = rs.getString("event_type");
    String eventData = rs.getString("event_data");
    String triggeringEvent = rs.getString("triggering_event");
    Optional<String> metadata = Optional.ofNullable(rs.getString("metadata"));
    return new EventAndTrigger(new EventIdTypeAndData(Int128.fromString(eventId), eventType, eventData, metadata), triggeringEvent);
  };

  @Override
  public <T extends Aggregate<T>> LoadedEvents find(String aggregateType,
                                                    String entityId,
                                                    Optional<AggregateCrudFindOptions> findOptions) {
    return eventuateTransactionTemplate.executeInTransaction(() -> findWithoutTransaction(aggregateType, entityId, findOptions));
  }

  private <T extends Aggregate<T>> LoadedEvents findWithoutTransaction(String aggregateType,
                                                                       String entityId,
                                                                       Optional<AggregateCrudFindOptions> findOptions) {
    String query = String.format("select snapshot_type, snapshot_json, entity_version, triggering_Events from %s where entity_type = ? and entity_id = ? order by entity_version desc", snapshotTable);

    query = eventuateSqlDialect.addLimitToSql(query, "1");

    Optional<LoadedSnapshot> snapshot =
            eventuateJdbcStatementExecutor
                    .query(query,
                            (rs, rownum) ->
                                    new LoadedSnapshot(
                                            new SerializedSnapshotWithVersion(
                                                    new SerializedSnapshot(rs.getString("snapshot_type"),
                                                            rs.getString("snapshot_json")),
                                                    Int128.fromString(rs.getString("entity_version"))),
                                            rs.getString("triggering_events")),
                            aggregateType,
                            entityId)
                    .stream()
                    .findFirst();


    List<EventAndTrigger> events;

    if (snapshot.isPresent()) {
      events = eventuateJdbcStatementExecutor.query(
              String.format("SELECT * FROM %s where entity_type = ? and entity_id = ? and event_id > ? order by event_id asc", eventTable),
              eventAndTriggerRowMapper, aggregateType, entityId, snapshot.get().getSerializedSnapshot().getEntityVersion().asString()
      );
    } else {
      events = eventuateJdbcStatementExecutor.query(
              String.format("SELECT * FROM %s where entity_type = ? and entity_id = ? order by event_id asc", eventTable),
              eventAndTriggerRowMapper, aggregateType, entityId
      );
    }

    logger.debug("Loaded {} events", events);
    Optional<EventAndTrigger> matching = findOptions.
            flatMap(AggregateCrudFindOptions::getTriggeringEvent).
            flatMap(te -> events.stream().filter(e -> te.getEventToken().equals(e.triggeringEvent)).findAny());
    if (matching.isPresent()) {
      throw new DuplicateTriggeringEventException();
    }
    if (!snapshot.isPresent() && events.isEmpty())
      throw new EntityNotFoundException(aggregateType, entityId);
    else {
      return new LoadedEvents(snapshot.map(LoadedSnapshot::getSerializedSnapshot), events.stream().map(e -> e.event).collect(Collectors.toList()));
    }
  }

  @Override
  public SaveUpdateResult update(EntityIdAndType entityIdAndType,
                                 Int128 entityVersion,
                                 List<EventTypeAndData> events,
                                 Optional<AggregateCrudUpdateOptions> updateOptions) {

    return eventuateTransactionTemplate.executeInTransaction(() -> updateWithoutTransaction(entityIdAndType, entityVersion, events, updateOptions));
  }

  @Override
  public SaveUpdateResult update(EntityIdAndType entityIdAndType,
                                 List<EventTypeAndData> events,
                                 Optional<AggregateCrudUpdateOptions> updateOptions) {

    return eventuateTransactionTemplate.executeInTransaction(() -> updateWithoutTransaction(entityIdAndType, events, updateOptions));
  }

  public SaveUpdateResult updateWithoutTransaction(EntityIdAndType entityIdAndType,
                                                   Int128 entityVersion,
                                                   List<EventTypeAndData> events,
                                                   Optional<AggregateCrudUpdateOptions> updateOptions) {

    // TODO - triggering event check

    String entityType = entityIdAndType.getEntityType();
    String aggregateType = entityIdAndType.getEntityType();

    String entityId = entityIdAndType.getEntityId();

    Optional<String> eventToken = updateOptions.flatMap(AggregateCrudUpdateOptions::getTriggeringEvent).map(EventContext::getEventToken);

    List<EventIdTypeAndData> eventsWithIds = insertEvents(events, entityId, entityType, eventToken);

    Int128 updatedEntityVersion = last(eventsWithIds).getId();

    int count = eventuateJdbcStatementExecutor.update(String.format("UPDATE %s SET entity_version = ? WHERE entity_type = ? and entity_id = ? and entity_version = ?", entityTable),
            updatedEntityVersion.asString(),
            entityType,
            entityId,
            entityVersion.asString()
    );

    if (count != 1) {
      logger.error("Failed to update entity: {}", count);
      throw new OptimisticLockingException(entityIdAndType, entityVersion);
    }

    updateOptions.flatMap(AggregateCrudUpdateOptions::getSnapshot).ifPresent(ss -> {

      String query = String.format("select snapshot_type, snapshot_json, entity_version, triggering_Events from %s where entity_type = ? and entity_id = ? order by entity_version desc", snapshotTable);

      query = eventuateSqlDialect.addLimitToSql(query, "1");

      Optional<LoadedSnapshot> previousSnapshot =
              eventuateJdbcStatementExecutor
                      .query(
                              query,
                              (rs, rownum) ->
                                      new LoadedSnapshot(
                                              new SerializedSnapshotWithVersion(
                                                      new SerializedSnapshot(rs.getString("snapshot_type"),
                                                              rs.getString("snapshot_json")),
                                                      Int128.fromString(rs.getString("entity_version"))),
                                              rs.getString("triggering_events")),
                              aggregateType,
                              entityId)
                      .stream()
                      .findFirst();


      List<EventAndTrigger> oldEvents;

      if (previousSnapshot.isPresent()) {
        oldEvents = eventuateJdbcStatementExecutor.query(
                String.format("SELECT * FROM %s where entity_type = ? and entity_id = ? and event_id > ? order by event_id asc", eventTable),
                eventAndTriggerRowMapper, aggregateType, entityId, previousSnapshot.get().getSerializedSnapshot().getEntityVersion().asString()
        );
      } else {
        oldEvents = eventuateJdbcStatementExecutor.query(
                String.format("SELECT * FROM %s where entity_type = ? and entity_id = ? order by event_id asc", eventTable),
                eventAndTriggerRowMapper, aggregateType, entityId
        );
      }

      String triggeringEvents = snapshotTriggeringEvents(previousSnapshot, oldEvents, updateOptions.flatMap(AggregateCrudUpdateOptions::getTriggeringEvent));

      eventuateJdbcStatementExecutor.update(String.format("INSERT INTO %s (entity_type, entity_id, entity_version, snapshot_type, snapshot_json, triggering_events) VALUES (?, ?, ?, ?, ?, ?)", snapshotTable),
              entityType,
              entityId,
              updatedEntityVersion.asString(),
              ss.getSnapshotType(),
              ss.getJson(),
              triggeringEvents);
    });

    return new SaveUpdateResult(new EntityIdVersionAndEventIds(entityId,
            updatedEntityVersion,
            eventsWithIds.stream().map(EventIdTypeAndData::getId).collect(Collectors.toList())),
            new PublishableEvents(aggregateType, entityId, eventsWithIds));

  }

  public SaveUpdateResult updateWithoutTransaction(EntityIdAndType entityIdAndType,
                                                   List<EventTypeAndData> events,
                                                   Optional<AggregateCrudUpdateOptions> updateOptions) {

    String entityType = entityIdAndType.getEntityType();
    String aggregateType = entityIdAndType.getEntityType();

    String entityId = entityIdAndType.getEntityId();


    Optional<String> eventToken = updateOptions.flatMap(AggregateCrudUpdateOptions::getTriggeringEvent).map(EventContext::getEventToken);

    List<EventIdTypeAndData> eventsWithIds = insertEvents(events, entityId, entityType, eventToken);

    Int128 updatedEntityVersion = last(eventsWithIds).getId();

    eventuateJdbcStatementExecutor.update(String.format("UPDATE %s SET entity_version = ? WHERE entity_type = ? and entity_id = ?", entityTable),
            updatedEntityVersion.asString(),
            entityType,
            entityId
    );

    return new SaveUpdateResult(new EntityIdVersionAndEventIds(entityId,
            updatedEntityVersion,
            eventsWithIds.stream().map(EventIdTypeAndData::getId).collect(Collectors.toList())),
            new PublishableEvents(aggregateType, entityId, eventsWithIds));
  }

  protected void checkSnapshotForDuplicateEvent(LoadedSnapshot ss, EventContext te) {
    // do nothing
  }

  protected String snapshotTriggeringEvents(Optional<LoadedSnapshot> previousSnapshot, List<EventAndTrigger> events, Optional<EventContext> eventContext) {
    // do nothing
    return null;
  }

  private List<EventIdTypeAndData> insertEvents(List<EventTypeAndData> events, String entityId, String entityType, Optional<String> eventToken) {
    List<EventIdTypeAndData> eventsWithIds = new ArrayList<>();

    for (EventTypeAndData event : events) {
      String eventId = eventuateCommonJdbcOperations.insertIntoEventsTable(idGenerator,
              entityId,
              event.getEventData(),
              event.getEventType(),
              entityType,
              eventToken,
              event.getMetadata(),
              eventuateSchema);

      eventsWithIds.add(new EventIdTypeAndData(Int128.fromString(eventId), event.getEventType(), event.getEventData(), event.getMetadata()));
    }

    return eventsWithIds;
  }
}
