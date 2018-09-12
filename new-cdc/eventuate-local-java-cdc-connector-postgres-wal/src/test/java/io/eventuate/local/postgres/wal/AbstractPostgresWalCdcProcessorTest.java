package io.eventuate.local.postgres.wal;

import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPostgresWalCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private PostgresWalClient postgresWalClient;

  @Autowired
  private OffsetStore offsetStore;

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new DbLogBasedCdcProcessor<>(postgresWalClient, offsetStore, new BinlogEntryToPublishedEventConverter());
  }

  @Override
  protected void onEventSent(PublishedEvent publishedEvent) {
    offsetStore.save(publishedEvent.getBinlogFileOffset());
  }
}
