package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.test.util.AbstractCdcTest;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

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

  @Autowired
  private CuratorFramework curatorFramework;

  @Test
  public void shouldGetEvents() throws InterruptedException {
    BinlogEntryToPublishedEventConverter binlogEntryToPublishedEventConverter = new BinlogEntryToPublishedEventConverter();

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    MySqlBinaryLogClient mySqlBinaryLogClient = new MySqlBinaryLogClient(eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            eventuateConfigurationProperties.getBinlogClientId(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            curatorFramework,
            eventuateConfigurationProperties.getLeadershipLockPath());

    EventuateLocalAggregateCrud localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    Consumer<BinlogEntry> consumer = binlogEntry ->
            publishedEvents.add(binlogEntryToPublishedEventConverter.convert(binlogEntry));

    MySqlBinlogEntryExtractor mySqlBinlogEntryExtractor = new MySqlBinlogEntryExtractor(dataSource, sourceTableNameSupplier.getSourceTableName(), eventuateSchema);

    MySqlBinlogEntryHandler binlogEntryHandler = new MySqlBinlogEntryHandler(JdbcUrlParser.parse(dataSourceURL).getDatabase(),
            eventuateSchema,
            mySqlBinlogEntryExtractor,
            sourceTableNameSupplier.getSourceTableName(),
            consumer);


    mySqlBinaryLogClient.addBinlogEntryHandler(binlogEntryHandler);

    mySqlBinaryLogClient.setBinlogFileOffset(Optional.empty());
    mySqlBinaryLogClient.start();

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
