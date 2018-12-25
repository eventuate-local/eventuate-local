package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.test.util.CdcProcessorTest;
import io.eventuate.local.test.util.SourceTableNameSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Consumer;

public abstract class AbstractPostgresWalCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  protected PostgresWalClient postgresWalClient;

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Value("${spring.datasource.username}")
  private String dbUserName;

  @Value("${spring.datasource.password}")
  private String dbPassword;

  @Override
  protected void prepareBinlogEntryHandler(Consumer<PublishedEvent> consumer) {

    postgresWalClient.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            new CdcDataPublisher<PublishedEvent>(null, null, null, null) {
              @Override
              public void handleEvent(PublishedEvent publishedEvent) throws EventuateLocalPublishingException {
                consumer.accept(publishedEvent);
              }
            });
  }

  @Override
  protected void startEventProcessing() {
    postgresWalClient.start();
  }

  @Override
  protected void stopEventProcessing() {
    postgresWalClient.stop();
  }
}
