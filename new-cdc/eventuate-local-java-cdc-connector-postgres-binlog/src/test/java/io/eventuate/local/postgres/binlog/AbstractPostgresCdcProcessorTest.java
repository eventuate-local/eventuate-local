package io.eventuate.local.postgres.binlog;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPostgresCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private PostgresBinaryLogClient<PublishedEvent> postgresBinaryLogClient;

  @Autowired
  private DatabaseLastSequenceNumberKafkaStore databaseLastSequenceNumberKafkaStore;

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new PostgresCdcProcessor<>(postgresBinaryLogClient, databaseLastSequenceNumberKafkaStore);
  }

  @Override
  protected void onEventSent(PublishedEvent publishedEvent) {
    databaseLastSequenceNumberKafkaStore.save(publishedEvent.getBinlogFileOffset());
  }
}
