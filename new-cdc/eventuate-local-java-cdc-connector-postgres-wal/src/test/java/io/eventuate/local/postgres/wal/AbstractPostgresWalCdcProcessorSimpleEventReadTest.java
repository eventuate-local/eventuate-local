package io.eventuate.local.postgres.wal;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.test.util.CdcProcessorSimpleEventReadTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPostgresWalCdcProcessorSimpleEventReadTest extends CdcProcessorSimpleEventReadTest {

  @Autowired
  private PostgresWalClient<PublishedEvent> postgresWalClient;

  @Autowired
  private OffsetStore offsetStore;

  @Override
  public CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new PostgresWalCdcProcessor<>(postgresWalClient, offsetStore);
  }

  @Override
  public void onEventSent(PublishedEvent publishedEvent) {
    offsetStore.save(publishedEvent.getBinlogFileOffset());
  }
}
