package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.test.util.AbstractCdcTest;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class AbstractPostgresWalCdcIntegrationTest extends AbstractCdcTest {

  @Value("${spring.datasource.url}")
  private String dataSourceURL;

  @Value("${spring.datasource.username}")
  private String dbUserName;

  @Value("${spring.datasource.password}")
  private String dbPassword;


  @Autowired
  private EventuateJdbcAccess eventuateJdbcAccess;

  @Autowired
  private EventuateConfigurationProperties eventuateConfigurationProperties;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private CuratorFramework curatorFramework;

  @Autowired
  private OffsetStore offsetStore;

  @Test
  public void shouldGetEvents() throws InterruptedException{
    PostgresWalClient postgresWalClient = new PostgresWalClient(dataSourceURL,
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

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    postgresWalClient.addBinlogEntryHandler(
            eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            new CdcDataPublisher<PublishedEvent>(null, null, null) {
              @Override
              public void handleEvent(PublishedEvent publishedEvent) throws EventuateLocalPublishingException {
                publishedEvents.add(publishedEvent);
              }
            });

    postgresWalClient.start();

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds saveResult = saveEvent(accountCreatedEventData);

    String accountDebitedEventData = generateAccountDebitedEvent();
    EntityIdVersionAndEventIds updateResult = updateEvent(saveResult.getEntityId(), saveResult.getEntityVersion(), accountDebitedEventData);

    LocalDateTime deadline = LocalDateTime.now().plusSeconds(20);

    waitForEvent(publishedEvents, saveResult.getEntityVersion(), deadline, accountCreatedEventData);
    waitForEvent(publishedEvents, updateResult.getEntityVersion(), deadline, accountDebitedEventData);
    postgresWalClient.stop();
  }

}
