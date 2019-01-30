package io.eventuate.local.cdc.debezium.migration;

import io.eventuate.javaclient.commonimpl.SerializedEvent;
import io.eventuate.testutil.Eventually;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MigrationE2ETestConfiguration.class)
@DirtiesContext
public class MigrationOldCdcPhaseE2ETest extends AbstractE2EMigrationTest  {

  @Test
  public void send2EventsAndReceive1() throws InterruptedException, ExecutionException {
    for (int i = 0; i < 2; i++) {
      sendEvent();
    }

    Handler handler = new Handler() {
      private boolean received;
      private boolean secondEventFailed;

      @Override
      public CompletableFuture<?> apply(SerializedEvent serializedEvent) {

        if (!received) {
          received = true;
          return super.apply(serializedEvent);
        } else {
          secondEventFailed = true;
          CompletableFuture<?> future = new CompletableFuture<>();
          future.completeExceptionally(new IllegalStateException());
          return future;
        }
      }

      @Override
      public void assertContainsEvent() throws InterruptedException {
        Eventually.eventually(() -> Assert.assertTrue(secondEventFailed));
        super.assertContainsEvent();
      }
    };

    subscribe(handler);
    handler.assertContainsEvent();
    eventuateKafkaAggregateSubscriptions.cleanUp();
    Thread.sleep(61000);
  }
}
