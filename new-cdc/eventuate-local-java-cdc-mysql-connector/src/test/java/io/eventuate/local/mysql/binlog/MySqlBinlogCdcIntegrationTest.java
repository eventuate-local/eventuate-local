package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MySqlBinlogCdcIntegrationTestConfiguration.class)
@IntegrationTest
public class MySqlBinlogCdcIntegrationTest extends AbstractCdcTest {

  @Value("${spring.datasource.url}")
  private String dataSourceURL;
  @Autowired
  EventuateJdbcAccess eventuateJdbcAccess;
  @Autowired
  private MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties;
  @Autowired
  private WriteRowsEventDataParser eventDataParser;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Test
  public void shouldGetEvents() throws IOException, TimeoutException, InterruptedException, ExecutionException {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient = new MySqlBinaryLogClient<>(eventDataParser,
            mySqlBinaryLogClientConfigurationProperties.getDbUserName(),
            mySqlBinaryLogClientConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            mySqlBinaryLogClientConfigurationProperties.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName(),
            mySqlBinaryLogClientConfigurationProperties.getMySqlBinLogClientName());

    EventuateLocalAggregateCrud localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    mySqlBinaryLogClient.start(Optional.empty(), publishedEvents::add);
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
