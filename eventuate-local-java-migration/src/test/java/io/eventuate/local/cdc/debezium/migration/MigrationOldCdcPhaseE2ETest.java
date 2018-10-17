package io.eventuate.local.cdc.debezium.migration;

import io.eventuate.Int128;
import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.local.java.jdbckafkastore.EventuateKafkaAggregateSubscriptions;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
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
import java.util.Optional;
import java.util.concurrent.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MigrationOldCdcPhaseE2ETest.Config.class)
@DirtiesContext
public class MigrationOldCdcPhaseE2ETest {

  @Configuration
  @EnableAutoConfiguration
  @Import(EventuateLocalConfiguration.class)
  public static class Config {
  }

  @Autowired
  private AggregateCrud eventuateJdbcEventStore;

  @Autowired
  private EventuateKafkaAggregateSubscriptions eventuateKafkaAggregateSubscriptions;

  boolean received;

  @Test
  public void test() throws InterruptedException, ExecutionException {

    String aggregateType = "TestAggregate_MIGRATION";
    String eventType = "TestEvent_MIGRATION";

    for (int i = 0; i < 2; i++) {
      eventuateJdbcEventStore.save(aggregateType,
              Collections.singletonList(new EventTypeAndData(eventType, "{}", Optional.empty())), Optional.empty());
    }

    BlockingQueue<Int128> result = new LinkedBlockingDeque<>();

    eventuateKafkaAggregateSubscriptions.subscribe("testSubscriber",
            Collections.singletonMap(aggregateType, Collections.singleton(eventType)),
            SubscriberOptions.DEFAULTS,
            se -> {
              if (!received) {
                received = true;
                result.add(se.getId());
                return CompletableFuture.completedFuture(null);
              } else {
                CompletableFuture<?> future = new CompletableFuture<>();
                future.completeExceptionally(new IllegalStateException());
                return future;
              }
            }).get();


    Assert.assertNotNull(result.poll(30, TimeUnit.SECONDS));

    Thread.sleep(15000);
    eventuateKafkaAggregateSubscriptions.cleanUp();
    Thread.sleep(15000);
  }
}
