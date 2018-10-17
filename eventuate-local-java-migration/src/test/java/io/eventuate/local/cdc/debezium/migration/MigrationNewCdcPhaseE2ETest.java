package io.eventuate.local.cdc.debezium.migration;

import io.eventuate.Int128;
import io.eventuate.SubscriberDurability;
import io.eventuate.SubscriberInitialPosition;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MigrationNewCdcPhaseE2ETest.Config.class)
@DirtiesContext
public class MigrationNewCdcPhaseE2ETest {


  @Configuration
  @EnableAutoConfiguration
  @Import(EventuateLocalConfiguration.class)
  public static class Config {
  }

  @Autowired
  private AggregateCrud eventuateJdbcEventStore;

  @Autowired
  private EventuateKafkaAggregateSubscriptions eventuateKafkaAggregateSubscriptions;

  @Test
  public void test() throws InterruptedException, ExecutionException {

    String aggregateType = "TestAggregate_MIGRATION";
    String eventType = "TestEvent_MIGRATION";

    BlockingQueue<Int128> result = new LinkedBlockingDeque<>();

    eventuateKafkaAggregateSubscriptions.subscribe("testSubscriber",
            Collections.singletonMap(aggregateType, Collections.singleton(eventType)),
            SubscriberOptions.DEFAULTS,
            se -> {
              result.add(se.getId());
              return CompletableFuture.completedFuture(null);
            }).get();

    Assert.assertNotNull(result.poll(30, TimeUnit.SECONDS));

    List<EventTypeAndData> events = Collections.singletonList(new EventTypeAndData(eventType, "{}", Optional.empty()));

    Int128 expectedEventId = AsyncUtil
            .await(eventuateJdbcEventStore.save(aggregateType, events, Optional.empty()))
            .getEntityVersion();

    Assert.assertEquals(expectedEventId, result.poll(30, TimeUnit.SECONDS));

    eventuateKafkaAggregateSubscriptions.cleanUp();
  }
}
