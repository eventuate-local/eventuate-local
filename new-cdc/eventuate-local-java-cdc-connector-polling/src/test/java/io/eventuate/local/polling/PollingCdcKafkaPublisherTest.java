package io.eventuate.local.polling;

import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PollingIntegrationTestConfiguration.class)
public class PollingCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Autowired
  private PollingDao pollingDao;

  @Before
  public void init() {
    super.init();

    pollingDao.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            publishingStrategy);

    pollingDao.start();
  }

  @Override
  public void clear() {
    pollingDao.stop();
  }
}
