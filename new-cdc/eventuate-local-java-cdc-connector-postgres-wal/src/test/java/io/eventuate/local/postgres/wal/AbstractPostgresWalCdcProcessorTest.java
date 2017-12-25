package io.eventuate.local.postgres.wal;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPostgresWalCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private PostgresWalClient<PublishedEvent> postgresWalClient;

  @Autowired
  private DatabaseOffsetKafkaStore databaseOffsetKafkaStore;

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new PostgresWalCdcProcessor<>(postgresWalClient, databaseOffsetKafkaStore);
  }

  @Override
  protected void onEventSent(PublishedEvent publishedEvent) {
    databaseOffsetKafkaStore.save(publishedEvent.getBinlogFileOffset());
  }
}
