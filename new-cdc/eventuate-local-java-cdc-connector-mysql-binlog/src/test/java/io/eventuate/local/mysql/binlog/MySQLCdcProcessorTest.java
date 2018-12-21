package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.CdcProcessingStatusService;
import io.eventuate.testutil.Eventually;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MySqlBinlogCdcIntegrationTestConfiguration.class,
        OffsetStoreMockConfiguration.class})
public class MySQLCdcProcessorTest extends AbstractMySQLCdcProcessorTest {

  @Test
  public void testMySqlCdcProcessingStatusService() {

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

    CdcProcessingStatusService cdcProcessingStatusService = mySqlBinaryLogClient.getCdcProcessingStatusService();

    Assert.assertFalse(mySqlBinaryLogClient.getCdcProcessingStatusService().getCurrentStatus().isCdcProcessingFinished());

    Eventually.eventually(() -> Assert.assertTrue(cdcProcessingStatusService.getCurrentStatus().isCdcProcessingFinished()));

    stopEventProcessing();
  }
}
