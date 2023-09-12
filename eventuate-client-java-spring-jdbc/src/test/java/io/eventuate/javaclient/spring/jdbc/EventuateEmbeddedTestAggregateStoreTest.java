package io.eventuate.javaclient.spring.jdbc;

import io.eventuate.*;
import io.eventuate.common.id.Int128;
import io.eventuate.javaclient.commonimpl.common.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.common.SerializedEvent;
import io.eventuate.javaclient.commonimpl.crud.*;
import io.eventuate.javaclient.commonimpl.crud.sync.AggregateCrud;
import io.eventuate.javaclient.commonimpl.events.sync.AggregateEvents;
import io.eventuate.javaclient.spring.EventuateJavaClientDomainConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Collections.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EventuateEmbeddedTestAggregateStoreTest.EventuateJdbcEventStoreTestConfiguration.class)
public class EventuateEmbeddedTestAggregateStoreTest {

  private final EventContext ectx = new EventContext("MyEventToken");
  private final String aggregateType = "MyAggregateType";

  @Autowired
  private AggregateCrud eventStore;

  @Autowired
  private AggregateEvents aggregateEvents;

  @Test
  public void findShouldCompleteWithDuplicateTriggeringEventException() throws ExecutionException, InterruptedException {
    EntityIdVersionAndEventIds eidv = eventStore.save(aggregateType,
            singletonList(new EventTypeAndData("MyEventType", "{}", Optional.empty())),
            Optional.of(new AggregateCrudSaveOptions().withEventContext(ectx)));
    shouldCompletedExceptionally(DuplicateTriggeringEventException.class, () -> eventStore.find(aggregateType, eidv.getEntityId(), Optional.of(new AggregateCrudFindOptions().withTriggeringEvent(ectx))));
  }

  @Test
  public void updateShouldCompleteWithOptimisticLockingException() throws ExecutionException, InterruptedException {
    EntityIdVersionAndEventIds eidv = eventStore.save(aggregateType,
            singletonList(new EventTypeAndData("MyEventType", "{}", Optional.empty())),
            Optional.of(new AggregateCrudSaveOptions().withEventContext(ectx)));
    shouldCompletedExceptionally(OptimisticLockingException.class, () -> eventStore.update(new EntityIdAndType(eidv.getEntityId(), aggregateType),
            new Int128(0,0), singletonList(new EventTypeAndData("MyEventType", "{}", Optional.empty())),
            Optional.of(new AggregateCrudUpdateOptions())));
  }


  private <T> void shouldCompletedExceptionally(Class<? extends Throwable> exceptionClass, Supplier<T> body)  {
    try {
      body.get();
      fail();
    } catch (Throwable e) {
      if (!exceptionClass.isInstance(e))
        throw e;
    }
  }

  @Configuration
  @Import({EmbeddedTestAggregateStoreConfiguration.class, EventuateJavaClientDomainConfiguration.class})
  @EnableAutoConfiguration
  public static class EventuateJdbcEventStoreTestConfiguration {

  }

  @Test
  public void shouldSaveAndLoadSnapshot() {

    EntityIdVersionAndEventIds eidv = eventStore.save(aggregateType,
            singletonList(new EventTypeAndData("MyEventType", "{}", Optional.empty())),
            Optional.of(new AggregateCrudSaveOptions().withEventContext(ectx)));

    EntityIdVersionAndEventIds updateResult = eventStore.update(
            new EntityIdAndType(eidv.getEntityId(), aggregateType),
            eidv.getEntityVersion(),
            singletonList(new EventTypeAndData("MyEventType", "{}", Optional.empty())),
            Optional.of(new AggregateCrudUpdateOptions().withSnapshot(new SerializedSnapshot("X", "Y"))));

    LoadedEvents findResult = eventStore.find(aggregateType, eidv.getEntityId(), Optional.of(new AggregateCrudFindOptions()));

    assertTrue(findResult.getSnapshot().isPresent());
    assertTrue(findResult.getEvents().isEmpty());

  }

  @Test
  public void shouldSaveAndLoadEventMetadata() throws InterruptedException {

    String saveMetadata = "MyMetaData";
    String updateMetadata = "MyMetaDataForUpdate";

    LinkedBlockingQueue<SerializedEvent> events = new LinkedBlockingQueue<>();

    aggregateEvents.subscribe("shouldSaveAndLoadEventMetadata",
            singletonMap(aggregateType, singleton("MyEventType")),
            new SubscriberOptions(), se -> {
              events.add(se);
              return new CompletableFuture<Object>();
            });

    EntityIdVersionAndEventIds eidv = eventStore.save(aggregateType,
            singletonList(new EventTypeAndData("MyEventType", "{}", Optional.of(saveMetadata))),
            Optional.empty());

    LoadedEvents findResult = eventStore.find(aggregateType, eidv.getEntityId(), Optional.of(new AggregateCrudFindOptions()));

    assertEquals(Optional.of(saveMetadata), findResult.getEvents().get(0).getMetadata());

    EntityIdVersionAndEventIds updateResult = eventStore.update(
            new EntityIdAndType(eidv.getEntityId(), aggregateType),
            eidv.getEntityVersion(),
            singletonList(new EventTypeAndData("MyEventType", "{}", Optional.of(updateMetadata))),
            Optional.empty());

    LoadedEvents findResult2 = eventStore.find(aggregateType, eidv.getEntityId(), Optional.of(new AggregateCrudFindOptions()));

    assertEquals(Optional.of(saveMetadata), findResult2.getEvents().get(0).getMetadata());
    assertEquals(Optional.of(updateMetadata), findResult2.getEvents().get(1).getMetadata());

    assertContainsEventWithMetadata(eidv.getEventIds().get(0), saveMetadata, events);
    assertContainsEventWithMetadata(updateResult.getEventIds().get(0), updateMetadata, events);
  }

  private void assertContainsEventWithMetadata(Int128 expectedEventId, String expectedMetadata, LinkedBlockingQueue<SerializedEvent> events) throws InterruptedException {
    long now = System.currentTimeMillis();
    long deadline = now + 10 * 1000;

    while (System.currentTimeMillis() < deadline) {
      SerializedEvent event = events.poll(100, TimeUnit.MILLISECONDS);
      if (event != null && event.getId().equals(expectedEventId)) {
        assertEquals(Optional.of(expectedMetadata), event.getMetadata());
        return;
      }
    }
    fail("could not find");
  }

  // TODO Save event with metadata, verify published
  // TODO Update event with metadata, load it, verify published
}