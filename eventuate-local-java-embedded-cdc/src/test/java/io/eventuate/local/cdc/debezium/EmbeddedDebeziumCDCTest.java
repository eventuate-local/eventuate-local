package io.eventuate.local.cdc.debezium;


import io.eventuate.EntityIdAndVersion;
import io.eventuate.Int128;
import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.local.java.jdbckafkastore.EventuateJdbcEventStoreConfiguration;
import io.eventuate.local.java.jdbckafkastore.EventuateKafkaAggregateSubscriptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EmbeddedDebeziumCDCTest.EmbeddedDebeziumCDCTestConfiguration.class)
@DirtiesContext
@IntegrationTest
public class EmbeddedDebeziumCDCTest {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @org.springframework.context.annotation.Configuration
  @Import({EventuateJdbcEventStoreConfiguration.class, EmbeddedDebeziumCDCConfiguration.class})
  @EnableAutoConfiguration
  public static class EmbeddedDebeziumCDCTestConfiguration {


  }

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

    EntityIdAndVersion ewidv = eventuateJdbcEventStore.save(aggregateType, myEvents, Optional.empty()).get();

    Int128 expectedEventId = ewidv.getEntityVersion();
    BlockingQueue<Int128> result = new LinkedBlockingDeque<>();

    logger.info("Looking for eventId {}", expectedEventId);

    eventuateKafkaAggregateSubscriptions.subscribe("testSubscriber",
            Collections.singletonMap(aggregateType, Collections.singleton(eventType)),
            SubscriberOptions.DEFAULTS,
            se -> {
              logger.info("got se {}", se);
              if (se.getId().equals(expectedEventId))
                result.add(se.getId());
              return CompletableFuture.completedFuture(null);
            }).get();

    assertNotNull(result.poll(30, TimeUnit.SECONDS));

    long endTime = System.currentTimeMillis();

    logger.info("got the event I just published in msecs {}", endTime - publishTime);
  }


}
