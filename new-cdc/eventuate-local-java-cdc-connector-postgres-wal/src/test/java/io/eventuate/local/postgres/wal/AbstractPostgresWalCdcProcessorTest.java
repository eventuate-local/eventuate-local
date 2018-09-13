package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractPostgresWalCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private PostgresWalClient postgresWalClient;

  @Autowired
  private OffsetStore offsetStore;

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new PostgresWalCdcProcessor<>(postgresWalClient,
            offsetStore,
            new BinlogEntryToPublishedEventConverter(),
            dataSourceUrl,
            sourceTableNameSupplier.getSourceTableName(),
            eventuateSchema);
  }

  @Override
  protected void onEventSent(PublishedEvent publishedEvent) {
    offsetStore.save(publishedEvent.getBinlogFileOffset());
  }
}
