package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.DuplicateTriggeringEventException;
import io.eventuate.EventContext;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.javaclient.spring.jdbc.EventAndTrigger;
import io.eventuate.javaclient.spring.jdbc.LoadedSnapshot;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SnapshotTriggeringEventsTest {

  private String a_1_98 = EtopEventContext.make("eventid", "topica", 1, 98).getEventToken();
  private String a_1_99 = EtopEventContext.make("eventid", "topica", 1, 99).getEventToken();
  private String a_1_100 = EtopEventContext.make("eventid", "topica", 1, 100).getEventToken();

  private String a_2_99 = EtopEventContext.make("eventid", "topica", 2, 99).getEventToken();

  @Test
  public void shouldNotRejectDifferent() {
    SnapshotTriggeringEvents ste = new SnapshotTriggeringEvents();
    ste.add(a_1_99);
    ste.checkForDuplicateEvent(EtopEventContext.decode(a_2_99).get());
  }

  @Test
  public void shouldNotRejectGreaterThan() {
    SnapshotTriggeringEvents ste = new SnapshotTriggeringEvents();
    ste.add(a_1_99);
    ste.checkForDuplicateEvent(EtopEventContext.decode(a_1_100).get());
  }

  @Test(expected = DuplicateTriggeringEventException.class)
  public void shouldRejectEquals() {
    SnapshotTriggeringEvents ste = new SnapshotTriggeringEvents();
    ste.add(a_1_99);
    ste.checkForDuplicateEvent(EtopEventContext.decode(a_1_99).get());
  }

  @Test(expected = DuplicateTriggeringEventException.class)
  public void shouldRejectLessThan() {
    SnapshotTriggeringEvents ste = new SnapshotTriggeringEvents();
    ste.add(a_1_99);
    ste.checkForDuplicateEvent(EtopEventContext.decode(a_1_98).get());
  }

  @Test
  public void shouldGetSnapshotTriggeringEvents() {
    SnapshotTriggeringEvents ste = SnapshotTriggeringEvents.getSnapshotTriggeringEvents(Optional.empty(), Collections.emptyList(), Optional.empty());
    assertTrue(ste.isEmpty());
  }

  @Test
  public void shouldGetSnapshotTriggeringEventsWithPreviousSnapshot() {
    SnapshotTriggeringEvents ste1 = new SnapshotTriggeringEvents();
    ste1.add(a_1_99);
    String triggeringEvents = JSonMapper.toJson(ste1);

    Optional<LoadedSnapshot> previousSnapshot = Optional.of(new LoadedSnapshot(null, triggeringEvents));
    List<EventAndTrigger> events = Collections.singletonList(new EventAndTrigger(null, a_1_100));
    Optional<EventContext> eventContext = Optional.of(new EventContext(a_2_99));

    SnapshotTriggeringEvents ste = SnapshotTriggeringEvents.getSnapshotTriggeringEvents(previousSnapshot, events, eventContext);
    assertFalse(ste.isEmpty());
  }

  @Test
  public void shouldSerde() {
    SnapshotTriggeringEvents ste = new SnapshotTriggeringEvents();
    String json = JSonMapper.toJson(ste);
    System.out.println("Json=" + json);
    SnapshotTriggeringEvents ste2 = JSonMapper.fromJson(json, SnapshotTriggeringEvents.class);
  }

  @Test
  public void shouldSerdeWithEvent() {
    SnapshotTriggeringEvents ste = new SnapshotTriggeringEvents();
    ste.add(a_1_99);
    String json = JSonMapper.toJson(ste);
    System.out.println("Json=" + json);
    SnapshotTriggeringEvents ste2 = JSonMapper.fromJson(json, SnapshotTriggeringEvents.class);
    System.out.println("ste2=" + ste2);
  }

  @Test
  public void shouldSerdeWithEvent2() {
    SnapshotTriggeringEvents ste = new SnapshotTriggeringEvents();
    ste.add(a_1_99);
    ste.add(a_2_99);
    String json = JSonMapper.toJson(ste);
    System.out.println("Json=" + json);
    SnapshotTriggeringEvents ste2 = JSonMapper.fromJson(json, SnapshotTriggeringEvents.class);
    System.out.println("ste2=" + ste2);
  }

}