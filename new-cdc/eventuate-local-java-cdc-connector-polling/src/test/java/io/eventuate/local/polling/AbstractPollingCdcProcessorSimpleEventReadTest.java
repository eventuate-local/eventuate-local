package io.eventuate.local.polling;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.test.util.CdcProcessorSimpleEventReadTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPollingCdcProcessorSimpleEventReadTest extends CdcProcessorSimpleEventReadTest {

  @Autowired
  private PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao;

  @Override
  public CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new PollingCdcProcessor<>(pollingDao, 500);
  }
}
