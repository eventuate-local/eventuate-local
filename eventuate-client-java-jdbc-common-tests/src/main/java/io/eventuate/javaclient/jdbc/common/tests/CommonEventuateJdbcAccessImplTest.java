package io.eventuate.javaclient.jdbc.common.tests;

import io.eventuate.EntityIdAndType;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrudUpdateOptions;
import io.eventuate.javaclient.commonimpl.crud.LoadedEvents;
import io.eventuate.javaclient.commonimpl.crud.SerializedSnapshot;
import io.eventuate.javaclient.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.jdbc.SaveUpdateResult;
import org.junit.Assert;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public abstract class CommonEventuateJdbcAccessImplTest {

  private static final String testAggregate = "testAggregate1";
  private static final String testEventType = "testEventType1";
  private static final String testEventData = "testEventData1";

  protected abstract EventuateJdbcStatementExecutor getEventuateJdbcStatementExecutor();
  protected abstract EventuateJdbcAccess getEventuateJdbcAccess();

  protected abstract String readAllEventsSql();
  protected abstract String readAllEntitiesSql();
  protected abstract String readAllSnapshots();
  protected abstract EventuateSchema getEventuateSchema();

  protected void clear() {
    getEventuateJdbcStatementExecutor().update(String.format("delete from %s", getEventuateSchema().qualifyTable("events")));
    getEventuateJdbcStatementExecutor().update(String.format("delete from %s", getEventuateSchema().qualifyTable("entities")));
    getEventuateJdbcStatementExecutor().update(String.format("delete from %s", getEventuateSchema().qualifyTable("snapshots")));
  }

  public void testSave() {
    EventTypeAndData eventTypeAndData = new EventTypeAndData(testEventType, testEventData, Optional.empty());

    getEventuateJdbcAccess().save(testAggregate, Collections.singletonList(eventTypeAndData), Optional.empty());

    List<Map<String, Object>> events = getEventuateJdbcStatementExecutor().queryForList(readAllEventsSql());
    Assert.assertEquals(1, events.size());

    List<Map<String, Object>> entities = getEventuateJdbcStatementExecutor().queryForList(readAllEntitiesSql());
    Assert.assertEquals(1, entities.size());
  }

  public void testFind() {
    EventTypeAndData eventTypeAndData = new EventTypeAndData(testEventType, testEventData, Optional.empty());

    SaveUpdateResult saveUpdateResult = getEventuateJdbcAccess().save(testAggregate, Collections.singletonList(eventTypeAndData), Optional.empty());

    LoadedEvents loadedEvents = getEventuateJdbcAccess().find(testAggregate, saveUpdateResult.getEntityIdVersionAndEventIds().getEntityId(), Optional.empty());

    Assert.assertEquals(1, loadedEvents.getEvents().size());
  }

  public void testUpdate() {
    EventTypeAndData eventTypeAndData = new EventTypeAndData(testEventType, testEventData, Optional.empty());
    SaveUpdateResult saveUpdateResult = getEventuateJdbcAccess().save(testAggregate, Collections.singletonList(eventTypeAndData), Optional.empty());


    EntityIdAndType entityIdAndType = new EntityIdAndType(saveUpdateResult.getEntityIdVersionAndEventIds().getEntityId(), testAggregate);
    eventTypeAndData = new EventTypeAndData("testEventType2", "testEventData2", Optional.empty());

    getEventuateJdbcAccess().update(entityIdAndType,
            saveUpdateResult.getEntityIdVersionAndEventIds().getEntityVersion(),
            Collections.singletonList(eventTypeAndData), Optional.of(new AggregateCrudUpdateOptions(Optional.empty(), Optional.of(new SerializedSnapshot("", "")))));

    List<Map<String, Object>> events = getEventuateJdbcStatementExecutor().queryForList(readAllEventsSql());
    Assert.assertEquals(2, events.size());

    List<Map<String, Object>> entities = getEventuateJdbcStatementExecutor().queryForList(readAllEntitiesSql());
    Assert.assertEquals(1, entities.size());

    List<Map<String, Object>> snapshots = getEventuateJdbcStatementExecutor().queryForList(readAllSnapshots());
    Assert.assertEquals(1, snapshots.size());

    LoadedEvents loadedEvents = getEventuateJdbcAccess().find(testAggregate, saveUpdateResult.getEntityIdVersionAndEventIds().getEntityId(), Optional.empty());
    Assert.assertTrue(loadedEvents.getSnapshot().isPresent());
  }

  protected List<String> loadSqlScriptAsListOfLines(String script)  {
    script = wrapClasspathResource(script);

    try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(script)))) {
      return bufferedReader.lines().collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void executeSql(List<String> sqlList) {
    getEventuateJdbcStatementExecutor().update(sqlList.stream().collect(Collectors.joining("\n")));
  }

  private String wrapClasspathResource(String file) {
    if (file.startsWith("/")) {
      return file;
    }

    return "/" + file;
  }
}
