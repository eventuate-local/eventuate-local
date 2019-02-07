package io.eventuate.local.cdc.debezium.migration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MigrationE2ETestConfiguration.class)
@DirtiesContext
public class MigrationNewCdcPhaseE2ETest extends AbstractE2EMigrationTest {

  @Test
  public void readEventFromTheOldCdc() throws InterruptedException, ExecutionException {
    Handler handler = new Handler();
    subscribe(handler);
    handler.assertContainsEvent();
    handler.assertContainsEventWithId(sendEvent());

    eventuateKafkaAggregateSubscriptions.cleanUp();
  }
}
