package io.eventuate.javaclient.commonimpl.common.schema;

import io.eventuate.common.id.Int128;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.javaclient.commonimpl.common.EventIdTypeAndData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DefaultEventuateEventSchemaManagerTest {


  private EventUpcaster upcaster1;
  private EventUpcaster upcaster2;
  private EventUpcaster upcaster3;
  private EventUpcaster upcaster4;
  private EventUpcaster upcaster5;
  private DefaultEventuateEventSchemaManager eventSchemaManager;
  private List<EventIdTypeAndData> unchanged;

  @Before
  public void setUp() {
    upcaster1 = mockUpcaster();
    upcaster2 = mockUpcaster();
    upcaster3 = mockUpcaster();
    upcaster4 = mockUpcaster();
    upcaster5 = mockUpcaster();

    eventSchemaManager = new DefaultEventuateEventSchemaManager();

    ConfigurableEventSchema configuration = new ConfigurableEventSchema(eventSchemaManager);

    configuration
            .forAggregate("Account")
            .version("1.0")
            .rename("OldEventName", "NewEventNameA")
            .transform("NewEventNameA", upcaster1)
            .transform("NewEventNameB", upcaster2)
            .version("2.0")
            .transform("SomeEvent", upcaster3)
            .transform("NewEventNameB", upcaster4)

            .forAggregate("Order")
            .version("1.0")
            .transform("SomeOtherEventName", upcaster5)
            .customize();

    unchanged = makeEvents("SomeEventX");

  }

  private EventUpcaster mockUpcaster() {
    EventUpcaster uc = mock(EventUpcaster.class);
    when(uc.upcast(any())).thenAnswer(returnsFirstArg());
    return uc;
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(upcaster1, upcaster2, upcaster3, upcaster4, upcaster5);
  }

  @Test
  public void shouldReturnLatestVersion() {
    assertEquals("2.0", eventSchemaManager.currentVersion("Account").get());
  }

  @Test
  public void shouldReturnEmptyForUnknownType() {
    assertEquals(Optional.empty(), eventSchemaManager.currentVersion("AccountUnknown"));
  }

  @Test
  public void shouldUpcastAndRename() {
    List<EventIdTypeAndData> eventsForOldEventName = makeEvents("OldEventName");

    List<EventIdTypeAndData> eventsForOldEventNameOutcome = makeEvents("NewEventNameA", eventSchemaManager.currentVersion("Account"));

    // Input: Account OldEventName null
    // Output:  rename=NewEventNameA upcasters=[upcaster1]

    assertEquals(eventsForOldEventNameOutcome, eventSchemaManager.upcastEvents("Account", eventsForOldEventName));
    verify(upcaster1).upcast(any());
  }

  @Test
  public void shouldUpcastTwice() {
    List<EventIdTypeAndData> eventsForOldEventName = makeEvents("NewEventNameB");

    List<EventIdTypeAndData> eventsForOldEventNameOutcome = makeEvents("NewEventNameB", eventSchemaManager.currentVersion("Account"));

    assertEquals(eventsForOldEventNameOutcome, eventSchemaManager.upcastEvents("Account", eventsForOldEventName));
    verify(upcaster2).upcast(any());
    verify(upcaster4).upcast(any());
  }

  @Test
  public void shouldUpcastOnce() {

    List<EventIdTypeAndData> eventsForOldEventName = makeEvents("NewEventNameB", "1.0");

    List<EventIdTypeAndData> eventsForOldEventNameOutcome = makeEvents("NewEventNameB", eventSchemaManager.currentVersion("Account"));

    assertEquals(eventsForOldEventNameOutcome, eventSchemaManager.upcastEvents("Account", eventsForOldEventName));
    verify(upcaster4).upcast(any());

    // Input: Account SomeEvent null
    // Output:  upcasters=[upcaster3]

    // Input: Account SomeEvent 2.0
    // Output:  upcasters=[]

  }

  private List<EventIdTypeAndData> makeEvents(String eventType) {
    return makeEvents(eventType, Optional.empty());
  }

  private List<EventIdTypeAndData> makeEvents(String eventType, String version) {
    return makeEvents(eventType, Optional.of(version));
  }

  private List<EventIdTypeAndData> makeEvents(String eventType, Optional<String> version) {
    Optional<String> metadata = version.map(v -> JSonMapper.toJson(singletonMap(DefaultEventuateEventSchemaManager.SCHEMA_VERSION, v)));
    return Collections.singletonList(
            new EventIdTypeAndData(
                    new Int128(0L, 0L),
                    eventType,
                    "{}",
                    metadata));
  }

  @Test
  public void shouldPassThroughUnknownEventType() {
    assertEquals(unchanged, eventSchemaManager.upcastEvents("Account", unchanged));
  }

  @Test
  public void shouldPassThroughUnknownAggregateType() {
    assertEquals(unchanged, eventSchemaManager.upcastEvents("SomeOtherAggregate", unchanged));
  }

}