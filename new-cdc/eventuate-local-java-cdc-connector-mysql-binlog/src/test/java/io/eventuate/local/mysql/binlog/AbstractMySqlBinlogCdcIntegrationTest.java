package io.eventuate.local.mysql.binlog;

import io.eventuate.Int128;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.javaclient.spring.jdbc.SaveUpdateResult;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.common.JdbcUrl;
import io.eventuate.local.common.JdbcUrlParser;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalJdbcAccess;
import io.eventuate.local.test.util.AbstractCdcTest;
import io.eventuate.local.testutil.CustomDBCreator;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public abstract class AbstractMySqlBinlogCdcIntegrationTest extends AbstractCdcTest {

  @Value("${spring.datasource.url}")
  private String dataSourceURL;

  @Autowired
  private EventuateConfigurationProperties eventuateConfigurationProperties;

  @Autowired
  private WriteRowsEventDataParser eventDataParser;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Autowired
  private EventuateSchema eventuateSchema;

  private String dataFile = "../../mysql/initialize-database.sql";

  @Value("${spring.datasource.driver.class.name}")
  private String driverClassName;

  @Autowired
  private JdbcTemplate jdbcTemplate;


  @Test
  public void shouldGetEvents() throws InterruptedException {
    MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = makeMySqlBinaryLogClient();
    try {
      BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

      mySqlBinaryLogClient.start(Optional.empty(), publishedEvents::add);
      String accountCreatedEventData = generateAccountCreatedEvent();
      EntityIdVersionAndEventIds saveResult = saveEvent(accountCreatedEventData);

      String accountDebitedEventData = generateAccountDebitedEvent();
      EntityIdVersionAndEventIds updateResult = updateEvent(saveResult.getEntityId(), saveResult.getEntityVersion(), accountDebitedEventData);

      // Wait for 10 seconds
      LocalDateTime deadline = LocalDateTime.now().plusSeconds(10);

      waitForEvent(publishedEvents, saveResult.getEntityVersion(), deadline, accountCreatedEventData);
      waitForEvent(publishedEvents, updateResult.getEntityVersion(), deadline, accountDebitedEventData);
    } finally {
      mySqlBinaryLogClient.stop();
    }
  }

  private MySqlBinaryLogClient<PublishedEvent> makeMySqlBinaryLogClient() {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    return new MySqlBinaryLogClient<>(eventDataParser,
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            eventuateConfigurationProperties.getBinlogClientId(),
            ResolvedEventuateSchema.make(eventuateSchema, jdbcUrl), sourceTableNameSupplier.getSourceTableName(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());
  }

  @Test
  public void shouldGetEventsFromOnlyEventuateSchema() throws InterruptedException {
    MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = makeMySqlBinaryLogClient();

    String otherSchemaName = "custom" + System.currentTimeMillis();

    createOtherSchema(otherSchemaName);

    SaveUpdateResult otherSaveResult = insertEventIntoOtherSchema(otherSchemaName);

    try {
      BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

      mySqlBinaryLogClient.start(Optional.empty(), publishedEvents::add);
      String accountCreatedEventData = generateAccountCreatedEvent();
      EntityIdVersionAndEventIds saveResult = saveEvent(accountCreatedEventData);

      LocalDateTime deadline = LocalDateTime.now().plusSeconds(10);

      Int128 eventId = saveResult.getEntityVersion();
      String eventData = accountCreatedEventData;

      boolean foundEvent = false;

      while (!foundEvent && LocalDateTime.now().isBefore(deadline)) {
        long millis = ChronoUnit.MILLIS.between(deadline, LocalDateTime.now());
        PublishedEvent event = publishedEvents.poll(millis, TimeUnit.MILLISECONDS);
        if (event != null) {
          System.out.println("Got: " + event);
          if (event.getId().equals(eventId.asString()) && eventData.equals(event.getEventData())) {
              foundEvent = true;
          } else if (event.getId().equals(otherSaveResult.getEntityIdVersionAndEventIds().getEventIds().get(0).asString())) {
              fail("Found event inserted into other schema");
          }
        }
      }
      if (!foundEvent)
        throw new RuntimeException("event not found: " + eventId);

    } finally {
      mySqlBinaryLogClient.stop();
    }
  }

  private SaveUpdateResult insertEventIntoOtherSchema(String otherSchemaName) {
    EventuateLocalJdbcAccess eventuateLocalJdbcAccess = new EventuateLocalJdbcAccess(jdbcTemplate, new EventuateSchema(otherSchemaName));

    return eventuateLocalJdbcAccess.save(Account.class.getName(), Collections.singletonList(new EventTypeAndData("Other-" + AccountCreatedEvent.class.getTypeName(), generateAccountCreatedEvent(), Optional.empty())), Optional.empty());
  }

  private void createOtherSchema(String otherSchemaName) {
    CustomDBCreator dbCreator = new CustomDBCreator(dataFile, dataSourceURL, driverClassName, eventuateConfigurationProperties.getDbUserName(), eventuateConfigurationProperties.getDbPassword());
    dbCreator.create(sqlList -> {
      sqlList.set(0, sqlList.get(0).replace("create database", "create database if not exists"));
      for (int i = 0; i < 3; i++) sqlList.set(i, sqlList.get(i).replace("eventuate", otherSchemaName));
      return sqlList;
    });
  }

}
