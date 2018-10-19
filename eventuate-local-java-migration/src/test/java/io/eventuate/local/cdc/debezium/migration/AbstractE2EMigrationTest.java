package io.eventuate.local.cdc.debezium.migration;

import io.eventuate.Int128;
import io.eventuate.SubscriberOptions;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.javaclient.commonimpl.SerializedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateKafkaAggregateSubscriptions;
import io.eventuate.testutil.AsyncUtil;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

public abstract class AbstractE2EMigrationTest {

  protected static final String aggregateType = "TestAggregate_MIGRATION";
  protected static final String eventType = "TestEvent_MIGRATION";

  @Autowired
  protected AggregateCrud eventuateJdbcEventStore;

  @Autowired
  protected EventuateKafkaAggregateSubscriptions eventuateKafkaAggregateSubscriptions;

  protected void subscribe(Function<SerializedEvent, CompletableFuture<?>> handler) throws InterruptedException, ExecutionException {
    eventuateKafkaAggregateSubscriptions.subscribe("testSubscriber",
            Collections.singletonMap(aggregateType, Collections.singleton(eventType)),
            SubscriberOptions.DEFAULTS,
            handler).get();
  }

  protected Int128 sendEvent() {
    return AsyncUtil
            .await(eventuateJdbcEventStore.save(aggregateType,
                    Collections.singletonList(new EventTypeAndData(eventType, "{}", Optional.empty())),
                    Optional.empty()))
            .getEntityVersion();
  }

  protected static class Handler implements Function<SerializedEvent, CompletableFuture<?>> {
    protected BlockingQueue<Int128> events = new LinkedBlockingDeque<>();

    @Override
    public CompletableFuture<?> apply(SerializedEvent serializedEvent) {
      events.add(serializedEvent.getId());
      return CompletableFuture.completedFuture(null);
    }

    public void assertContainsEvent() throws InterruptedException {
      Assert.assertNotNull(events.poll(30, TimeUnit.SECONDS));
    }

    public void assertContainsEventWithId(Int128 id) throws InterruptedException {
      Assert.assertEquals(id, events.poll(30, TimeUnit.SECONDS));
    }
  }
}
