package io.eventuate.javaclient.jdbc;

import io.eventuate.Aggregate;
import io.eventuate.DuplicateTriggeringEventException;
import io.eventuate.EntityAlreadyExistsException;
import io.eventuate.EntityIdAndType;
import io.eventuate.EntityNotFoundException;
import io.eventuate.EventContext;
import io.eventuate.OptimisticLockingException;
import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.id.IdGeneratorImpl;
import io.eventuate.common.id.Int128;
import io.eventuate.common.jdbc.*;
import io.eventuate.javaclient.commonimpl.common.EventIdTypeAndData;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.crud.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventuateJdbcAccessImpl implements EventuateJdbcAccess {
  protected Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateTransactionTemplate eventuateTransactionTemplate;
  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;
  private String entityTable;
  private String eventTable;
  private String snapshotTable;

  private EventuateCommonJdbcOperations eventuateCommonJdbcOperations;
  private EventuateSchema eventuateSchema;

  public EventuateJdbcAccessImpl(EventuateTransactionTemplate eventuateTransactionTemplate,
                                 EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                 EventuateCommonJdbcOperations eventuateCommonJdbcOperations) {

    this(eventuateTransactionTemplate, eventuateJdbcStatementExecutor, eventuateCommonJdbcOperations, new EventuateSchema());
  }

  public EventuateJdbcAccessImpl(EventuateTransactionTemplate eventuateTransactionTemplate,
                                 EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                 EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                 EventuateSchema eventuateSchema) {

    this.eventuateTransactionTemplate = eventuateTransactionTemplate;
    this.eventuateJdbcStatementExecutor = eventuateJdbcStatementExecutor;
    this.eventuateCommonJdbcOperations = eventuateCommonJdbcOperations;
    this.eventuateSchema = eventuateSchema;

    entityTable = eventuateSchema.qualifyTable("entities");
    eventTable = eventuateSchema.qualifyTable("events");
    snapshotTable = eventuateSchema.qualifyTable("snapshots");
  }

  private IdGenerator idGenerator = new IdGeneratorImpl();

  @Override
  public SaveUpdateResult save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> saveOptions) {
    return eventuateTransactionTemplate.executeInTransaction(() -> saveWithoutTransaction(aggregateType, events, saveOptions));
  }

  private SaveUpdateResult saveWithoutTransaction(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> saveOptions) {
    List<EventIdTypeAndData> eventsWithIds = events.stream().map(this::toEventWithId).collect(Collectors.toList());
    String entityId = saveOptions.flatMap(AggregateCrudSaveOptions::getEntityId).orElse(idGenerator.genId().asString());

    Int128 entityVersion = last(eventsWithIds).getId();

    try {
      eventuateJdbcStatementExecutor.update(String.format("INSERT INTO %s (entity_type, entity_id, entity_version) VALUES (?, ?, ?)", entityTable),
              aggregateType, entityId, entityVersion.asString());
    } catch (EventuateDuplicateKeyException e) {
      throw new EntityAlreadyExistsException();
    }


    for (EventIdTypeAndData event : eventsWithIds) {
      eventuateCommonJdbcOperations.insertIntoEventsTable(event.getId().asString(),
              entityId,
              event.getEventData(),
              event.getEventType(),
              aggregateType,
              saveOptions.flatMap(AggregateCrudSaveOptions::getTriggeringEvent).map(EventContext::getEventToken),
              event.getMetadata(),
              eventuateSchema);
    }


    return new SaveUpdateResult(new EntityIdVersionAndEventIds(entityId, entityVersion, eventsWithIds.stream().map(EventIdTypeAndData::getId).collect(Collectors.toList())),
            new PublishableEvents(aggregateType, entityId, eventsWithIds));
  }


  private <T> T last(List<T> eventsWithIds) {
    return eventsWithIds.get(eventsWithIds.size() - 1);
  }

  private EventIdTypeAndData toEventWithId(EventTypeAndData eventTypeAndData) {
    return new EventIdTypeAndData(idGenerator.genId(), eventTypeAndData.getEventType(), eventTypeAndData.getEventData(), eventTypeAndData.getMetadata());
  }

  private final EventuateRowMapper<EventAndTrigger> eventAndTriggerRowMapper = (rs, rowNum) -> {
    String eventId = rs.getString("event_id");
    String eventType = rs.getString("event_type");
    String eventData = rs.getString("event_data");
    String entityId1 = rs.getString("entity_id");
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
    Optional<LoadedSnapshot> snapshot =
            eventuateJdbcStatementExecutor
                    .query(
                            String.format("select snapshot_type, snapshot_json, entity_version, triggering_Events from %s where entity_type = ? and entity_id = ? order by entity_version desc LIMIT 1", snapshotTable),
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

  public SaveUpdateResult updateWithoutTransaction(EntityIdAndType entityIdAndType,
                                 Int128 entityVersion,
                                 List<EventTypeAndData> events,
                                 Optional<AggregateCrudUpdateOptions> updateOptions) {

    // TODO - triggering event check

    List<EventIdTypeAndData> eventsWithIds = events.stream().map(this::toEventWithId).collect(Collectors.toList());

    String entityType = entityIdAndType.getEntityType();
    String aggregateType = entityIdAndType.getEntityType();

    String entityId = entityIdAndType.getEntityId();

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

      Optional<LoadedSnapshot> previousSnapshot =
              eventuateJdbcStatementExecutor
                      .query(
                              String.format("select snapshot_type, snapshot_json, entity_version, triggering_Events from %s where entity_type = ? and entity_id = ? order by entity_version desc LIMIT 1", snapshotTable),
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


    for (EventIdTypeAndData event : eventsWithIds) {
      eventuateCommonJdbcOperations.insertIntoEventsTable(event.getId().asString(),
              entityId,
              event.getEventData(),
              event.getEventType(),
              entityType,
              updateOptions.flatMap(AggregateCrudUpdateOptions::getTriggeringEvent).map(EventContext::getEventToken),
              event.getMetadata(),
              eventuateSchema);
    }


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



}
