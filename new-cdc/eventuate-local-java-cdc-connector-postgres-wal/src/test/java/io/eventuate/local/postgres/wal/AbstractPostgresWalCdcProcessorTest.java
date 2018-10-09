package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.test.common.OffsetStoreMock;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Consumer;

public abstract class AbstractPostgresWalCdcProcessorTest extends CdcProcessorTest {

  private PostgresWalClient postgresWalClient;

  private OffsetStore offsetStore = new OffsetStoreMock();

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

  @Autowired
  private EventuateConfigurationProperties eventuateConfigurationProperties;

  @Autowired
  private CuratorFramework curatorFramework;

  @Override
  @Before
  public void init() {
    super.init();

    postgresWalClient = new PostgresWalClient(dataSourceUrl,
            dbUserName,
            dbPassword,
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            eventuateConfigurationProperties.getPostgresWalIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPostgresReplicationSlotName(),
            curatorFramework,
            eventuateConfigurationProperties.getLeadershipLockPath(),
            offsetStore);
  }

  @Override
  protected void prepareBinlogEntryHandler(Consumer<PublishedEvent> consumer) {

    postgresWalClient.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            new CdcDataPublisher<PublishedEvent>(null, null, null) {
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

  @Override
  public void onEventSent(PublishedEvent publishedEvent) {
    offsetStore.save(publishedEvent.getBinlogFileOffset().get());
  }
}
