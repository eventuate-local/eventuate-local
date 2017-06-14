package io.eventuate.local.cdc.debezium;

import io.eventuate.Int128;
import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.local.java.jdbckafkastore.EventuateKafkaAggregateSubscriptions;
import io.eventuate.testutil.AsyncUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

  @Test
  public void shouldCaptureAndPublishChange() throws ExecutionException, InterruptedException {

    String aggregateType = "TestAggregate";
    String eventType = "TestEvent";

    List<EventTypeAndData> myEvents = Collections.singletonList(new EventTypeAndData(eventType, "{}"));

    long publishTime = System.currentTimeMillis();

    EntityIdVersionAndEventIds ewidv = AsyncUtil.await(eventuateJdbcEventStore.save(aggregateType, myEvents, Optional.empty()));

    Int128 expectedEventId = ewidv.getEntityVersion();
    BlockingQueue<Int128> result = new LinkedBlockingDeque<>();

    logger.debug("Looking for eventId {}", expectedEventId);

    eventuateKafkaAggregateSubscriptions.subscribe("testSubscriber",
            Collections.singletonMap(aggregateType, Collections.singleton(eventType)),
            SubscriberOptions.DEFAULTS,
            se -> {
              logger.debug("got se {}", se);
              if (se.getId().equals(expectedEventId))
                result.add(se.getId());
              return CompletableFuture.completedFuture(null);
            }).get();

    Assert.assertNotNull(result.poll(30, TimeUnit.SECONDS));

    long endTime = System.currentTimeMillis();

    logger.debug("got the event I just published in msecs {}", endTime - publishTime);
  }

  @Test
  public void shouldStartup() throws InterruptedException {
    TimeUnit.SECONDS.sleep(10);
  }
}
