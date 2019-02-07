package io.eventuate.local.postgres.wal;

import io.eventuate.local.common.CdcProcessingStatus;
import io.eventuate.local.common.CdcProcessingStatusService;
import io.eventuate.testutil.Eventually;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

@ActiveProfiles("PostgresWal")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PostgresWalCdcIntegrationTestConfiguration.class)
public class PostgresWalCdcProcessorTest extends AbstractPostgresWalCdcProcessorTest {

  @Test
  public void testPostgresWalCdcProcessingStatusService() {

    prepareBinlogEntryHandler(publishedEvent -> {
      onEventSent(publishedEvent);
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    startEventProcessing();

    saveEvent(generateAccountCreatedEvent());
    saveEvent(generateAccountCreatedEvent());
    saveEvent(generateAccountCreatedEvent());

    CdcProcessingStatusService cdcProcessingStatusService = postgresWalClient.getCdcProcessingStatusService();

    Assert.assertFalse(cdcProcessingStatusService.getCurrentStatus().isCdcProcessingFinished());

    Eventually.eventually(60,
            500,
            TimeUnit.MILLISECONDS,
            () -> {
              CdcProcessingStatus currentStatus = cdcProcessingStatusService.getCurrentStatus();
              Assert.assertTrue(currentStatus.toString(), currentStatus.isCdcProcessingFinished());
            });

    stopEventProcessing();
  }
}
