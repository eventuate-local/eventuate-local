package io.eventuate.local.polling;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPollingCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao;

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new PollingCdcProcessor<>(pollingDao, 500);
  }
}
