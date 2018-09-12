package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.test.util.AbstractCdcTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class AbstractMySqlBinlogCdcIntegrationTest extends AbstractCdcTest {

  @Value("${spring.datasource.url}")
  private String dataSourceURL;

  @Autowired
  EventuateJdbcAccess eventuateJdbcAccess;

  @Autowired
  private EventuateConfigurationProperties eventuateConfigurationProperties;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Test
  public void shouldGetEvents() throws InterruptedException {
    BinlogEntryToPublishedEventConverter binlogEntryToPublishedEventConverter = new BinlogEntryToPublishedEventConverter();

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    MySqlBinaryLogClient mySqlBinaryLogClient = new MySqlBinaryLogClient(dataSource,
            eventuateSchema,
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            eventuateConfigurationProperties.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());

    EventuateLocalAggregateCrud localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    mySqlBinaryLogClient.start(Optional.empty(), binlogEntry ->
      publishedEvents.add(binlogEntryToPublishedEventConverter.convert(binlogEntry)));

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds saveResult = saveEvent(localAggregateCrud, accountCreatedEventData);

    String accountDebitedEventData = generateAccountDebitedEvent();
    EntityIdVersionAndEventIds updateResult = updateEvent(saveResult.getEntityId(), saveResult.getEntityVersion(), localAggregateCrud, accountDebitedEventData);

    // Wait for 10 seconds
    LocalDateTime deadline = LocalDateTime.now().plusSeconds(10);

    waitForEvent(publishedEvents, saveResult.getEntityVersion(), deadline, accountCreatedEventData);
    waitForEvent(publishedEvents, updateResult.getEntityVersion(), deadline, accountDebitedEventData);
    mySqlBinaryLogClient.stop();
  }

}
