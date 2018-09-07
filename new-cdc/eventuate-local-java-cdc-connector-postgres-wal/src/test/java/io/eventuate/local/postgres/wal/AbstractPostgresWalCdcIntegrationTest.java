package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.test.util.AbstractCdcTest;
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
  private EventuateConfigurationProperties eventuateConfigurationProperties;

  @Autowired
  private PostgresWalMessageParser postgresWalMessageParser;

  @Test
  public void shouldGetEvents() throws InterruptedException{
    PostgresWalClient<PublishedEvent> postgresWalClient = new PostgresWalClient<>(postgresWalMessageParser,
            dataSourceURL,
            dbUserName,
            dbPassword,
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            eventuateConfigurationProperties.getPostgresWalIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPostgresReplicationSlotName());

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    postgresWalClient.start(Optional.empty(), publishedEvents::add);
    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds saveResult = saveEvent(accountCreatedEventData);

    String accountDebitedEventData = generateAccountDebitedEvent();
    EntityIdVersionAndEventIds updateResult = updateEvent(saveResult.getEntityId(), saveResult.getEntityVersion(), accountDebitedEventData);

    // Wait for 10 seconds
    LocalDateTime deadline = LocalDateTime.now().plusSeconds(10);

    waitForEvent(publishedEvents, saveResult.getEntityVersion(), deadline, accountCreatedEventData);
    waitForEvent(publishedEvents, updateResult.getEntityVersion(), deadline, accountDebitedEventData);
    postgresWalClient.stop();
  }

}
