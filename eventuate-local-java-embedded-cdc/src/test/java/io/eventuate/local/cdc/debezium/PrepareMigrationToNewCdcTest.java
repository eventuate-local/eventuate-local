package io.eventuate.local.cdc.debezium;

import io.eventuate.Int128;
import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.local.java.jdbckafkastore.EventuateKafkaAggregateSubscriptions;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
import io.eventuate.testutil.AsyncUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PrepareMigrationToNewCdcTest.EventTableChangesToAggregateTopicRelayTestConfiguration.class)
@DirtiesContext
public class PrepareMigrationToNewCdcTest {

  @org.springframework.context.annotation.Configuration
  @Import({EventuateLocalConfiguration.class, EventTableChangesToAggregateTopicRelayConfiguration.class})
  @EnableAutoConfiguration
  public static class EventTableChangesToAggregateTopicRelayTestConfiguration {
  }

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private AggregateCrud eventuateJdbcEventStore;

  @Autowired
  private EventuateKafkaAggregateSubscriptions eventuateKafkaAggregateSubscriptions;

  @Test
  public void shouldCaptureAndPublishChange() throws ExecutionException, InterruptedException {

    String aggregateType = "TestAggregate_MIGRATION";
    String eventType = "TestEvent_MIGRATION";

    List<EventTypeAndData> myEvents = Collections.singletonList(new EventTypeAndData(eventType, "{}", Optional.empty()));

    EntityIdVersionAndEventIds ewidv = AsyncUtil.await(eventuateJdbcEventStore.save(aggregateType, myEvents, Optional.empty()));

    Int128 expectedEventId = ewidv.getEntityVersion();
    BlockingQueue<Int128> result = new LinkedBlockingDeque<>();

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
  }
}
