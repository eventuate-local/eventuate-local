package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.test.util.AbstractCdcTest;
import io.eventuate.local.test.util.SourceTableNameSupplier;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Optional;
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
  private PostgresWalClient postgresWalClient;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Test
  public void shouldGetEvents() throws InterruptedException{

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    postgresWalClient.addBinlogEntryHandler(
            eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            new CdcDataPublisher<PublishedEvent>(null, null, null, null) {
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
