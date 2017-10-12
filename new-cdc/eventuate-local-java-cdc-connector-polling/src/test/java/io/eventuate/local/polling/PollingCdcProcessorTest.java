package io.eventuate.local.polling;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PollingIntegrationTestConfiguration.class)
@IntegrationTest
public class PollingCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao;

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new PollingCdcProcessor<>(pollingDao, 500);
  }
}
