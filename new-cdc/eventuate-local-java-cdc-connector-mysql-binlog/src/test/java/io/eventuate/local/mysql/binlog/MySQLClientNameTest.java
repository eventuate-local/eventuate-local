package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.test.util.AbstractCdcTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MySqlBinlogCdcIntegrationTestConfiguration.class)
public class MySQLClientNameTest extends AbstractCdcTest {

  @Value("${spring.datasource.url}")
  private String dataSourceURL;

  @Autowired
  EventuateJdbcAccess eventuateJdbcAccess;

  @Autowired
  private EventuateConfigurationProperties eventuateConfigurationProperties;

  @Autowired
  private WriteRowsEventDataParser eventDataParser;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Autowired
  private EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;

  @Autowired
  private EventuateKafkaProducer eventuateKafkaProducer;

  private DatabaseOffsetKafkaStore databaseOffsetKafkaStore;

  @Autowired
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  @Test
  public void test() throws Exception {

    databaseOffsetKafkaStore = createDatabaseOffsetKafkaStore(createMySqlBinaryLogClient());

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();
    CdcProcessor<PublishedEvent> cdcProcessor = createMySQLCdcProcessor();
    cdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      databaseOffsetKafkaStore.save(publishedEvent.getBinlogFileOffset());
    });

    EventuateLocalAggregateCrud localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
    List<EventTypeAndData> events = Collections.singletonList(new EventTypeAndData("TestEvent", "{}", Optional.empty()));
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = localAggregateCrud.save("TestAggregate", events, Optional.empty());

    PublishedEvent publishedEvent;

    while((publishedEvent = publishedEvents.poll(10, TimeUnit.SECONDS)) != null) {
      if (entityIdVersionAndEventIds.getEntityVersion().asString().equals(publishedEvent.getId())) {
        break;
      }
    }

    Assert.assertEquals(entityIdVersionAndEventIds.getEntityVersion().asString(), publishedEvent.getId());

    cdcProcessor.stop();

    /*waiting while offset is storing in kafka*/
    Thread.sleep(10000);

    databaseOffsetKafkaStore = createDatabaseOffsetKafkaStore(createMySqlBinaryLogClient());

    cdcProcessor = createMySQLCdcProcessor();
    cdcProcessor.start(event -> {
      publishedEvents.add(event);
      databaseOffsetKafkaStore.save(event.getBinlogFileOffset());
    });

    while((publishedEvent = publishedEvents.poll(10, TimeUnit.SECONDS)) != null) {
        Assert.assertNotEquals(entityIdVersionAndEventIds.getEntityVersion().asString(), publishedEvent.getId());
    }
  }

  private CdcProcessor<PublishedEvent> createMySQLCdcProcessor() {
    MySqlBinaryLogClient mySqlBinaryLogClient = createMySqlBinaryLogClient();
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, createDatabaseOffsetKafkaStore(mySqlBinaryLogClient), debeziumBinlogOffsetKafkaStore);
  }

  public DatabaseOffsetKafkaStore createDatabaseOffsetKafkaStore(MySqlBinaryLogClient mySqlBinaryLogClient) {

    return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
        mySqlBinaryLogClient.getName(),
        eventuateKafkaProducer,
        eventuateKafkaConfigurationProperties);
  }

  private MySqlBinaryLogClient<PublishedEvent> createMySqlBinaryLogClient() {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    return new MySqlBinaryLogClient<>(eventDataParser,
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            eventuateConfigurationProperties.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());
  }
}
