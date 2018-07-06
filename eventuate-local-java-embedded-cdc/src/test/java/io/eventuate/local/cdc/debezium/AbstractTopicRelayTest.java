package io.eventuate.local.cdc.debezium;

import io.eventuate.Int128;
import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.java.jdbckafkastore.EventuateKafkaAggregateSubscriptions;
import io.eventuate.testutil.AsyncUtil;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTopicRelayTest {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private AggregateCrud eventuateJdbcEventStore;

  @Autowired
  private EventuateKafkaAggregateSubscriptions eventuateKafkaAggregateSubscriptions;

  @Autowired
  private EventTableChangesToAggregateTopicRelay eventTableChangesToAggregateTopicRelay;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Test
  public void shouldCaptureAndPublishChange() throws ExecutionException, InterruptedException {

    String aggregateType = uniqueId("TestAggregate");
    String eventType = uniqueId("TestEvent");

    List<EventTypeAndData> events = createTestEvent(eventType);

    long publishTime = System.currentTimeMillis();

    Int128 expectedEventId = saveEvents(aggregateType, events);

    logger.debug("Looking for eventId {}", expectedEventId);

    subscribe(uniqueId("testSubscriber-" + getClass().getName()), aggregateType, eventType, expectedEventId)
            .assertEventReceived();

    long endTime = System.currentTimeMillis();

    logger.debug("got the event I just published in msecs {}", endTime - publishTime);
  }

  @Test
  public void testEventTableCleanUp() throws ExecutionException, InterruptedException {

    String aggregateType = uniqueId("TestAggregate");
    String eventType = uniqueId("TestEvent");
    List<EventTypeAndData> event = createTestEvent(eventType);

    Int128 expectedEventId = saveEvents(aggregateType, event);

    subscribe(uniqueId("testSubscriber-" + getClass().getName()), aggregateType, eventType, expectedEventId)
            .assertEventReceived();

    deleteAllEvents();

    expectedEventId = saveEvents(aggregateType, event);

    subscribe(uniqueId("testSubscriber-" + getClass().getName()), aggregateType, eventType, expectedEventId)
            .assertEventReceived();
  }

  @Test
  public void shouldStartup() throws InterruptedException {
    TimeUnit.SECONDS.sleep(10);
  }

  private List<EventTypeAndData> createTestEvent(String eventType) {
    return Collections.singletonList(new EventTypeAndData(eventType, "{}", Optional.empty()));
  }

  private Int128 saveEvents(String aggregateType, List<EventTypeAndData> events) {
    return AsyncUtil.await(eventuateJdbcEventStore.save(aggregateType, events, Optional.empty())).getEntityVersion();
  }

  private String uniqueId(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private void deleteAllEvents() {
    new JdbcTemplate(dataSource).update(String.format("delete from %s", eventuateSchema.qualifyTable("events")));
  }

  private TestSubscriber subscribe(String subscriber, String aggregateType, String eventType, Int128 expectedEventId)
          throws InterruptedException, ExecutionException {

    TestSubscriber testSubscriber = new TestSubscriber();

    eventuateKafkaAggregateSubscriptions.cleanUp();

    eventuateKafkaAggregateSubscriptions.subscribe(subscriber,
            Collections.singletonMap(aggregateType, Collections.singleton(eventType)),
            SubscriberOptions.DEFAULTS,
            se -> {
              logger.debug("got se {}", se);
              if (expectedEventId.equals(se.getId()))
                testSubscriber.addReceivedEventId(se.getId());
              return CompletableFuture.completedFuture(null);
            }).get();

    return testSubscriber;
  }

  private static class TestSubscriber {
    private BlockingQueue<Int128> eventIds = new LinkedBlockingDeque<>();

    public void addReceivedEventId(Int128 id) {
      eventIds.add(id);
    }

    public void assertEventReceived() throws InterruptedException {
      Assert.assertNotNull(eventIds.poll(30, TimeUnit.SECONDS));
    }
  }
}
