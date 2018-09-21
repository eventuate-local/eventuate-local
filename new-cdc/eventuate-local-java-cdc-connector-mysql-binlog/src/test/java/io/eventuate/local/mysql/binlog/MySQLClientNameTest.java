package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.test.util.AbstractCdcTest;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
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
  private DataSource dataSource;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Autowired
  private EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;

  @Autowired
  private EventuateKafkaProducer eventuateKafkaProducer;

  @Autowired
  private CuratorFramework curatorFramework;

  private OffsetStore offsetStore;

  @Autowired
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  @Test
  public void test() throws Exception {

    offsetStore = createDatabaseOffsetKafkaStore(createMySqlBinaryLogClient());

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    MySqlBinaryLogClient mySqlBinaryLogClient = createMySqlBinaryLogClient();

    CdcProcessor<PublishedEvent> cdcProcessor = createMySQLCdcProcessor(mySqlBinaryLogClient);
    cdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      offsetStore.save(publishedEvent.getBinlogFileOffset());
    });
    mySqlBinaryLogClient.start();

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

    mySqlBinaryLogClient.stop();
    cdcProcessor.stop();

    offsetStore = createDatabaseOffsetKafkaStore(createMySqlBinaryLogClient());

    cdcProcessor = createMySQLCdcProcessor(mySqlBinaryLogClient);
    cdcProcessor.start(event -> {
      publishedEvents.add(event);
      offsetStore.save(event.getBinlogFileOffset());
    });
    mySqlBinaryLogClient.start();

    while((publishedEvent = publishedEvents.poll(10, TimeUnit.SECONDS)) != null) {
        Assert.assertNotEquals(entityIdVersionAndEventIds.getEntityVersion().asString(), publishedEvent.getId());
    }
  }

  private CdcProcessor<PublishedEvent> createMySQLCdcProcessor(MySqlBinaryLogClient mySqlBinaryLogClient) {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient,
            new BinlogEntryToPublishedEventConverter(),
            sourceTableNameSupplier.getSourceTableName(),
            eventuateSchema);
  }

  public DatabaseOffsetKafkaStore createDatabaseOffsetKafkaStore(MySqlBinaryLogClient mySqlBinaryLogClient) {

    return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
        mySqlBinaryLogClient.getName(),
        eventuateKafkaProducer,
        eventuateKafkaConfigurationProperties,
        EventuateKafkaConsumerConfigurationProperties.empty());
  }

  private MySqlBinaryLogClient createMySqlBinaryLogClient() {
    return new MySqlBinaryLogClient(
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            dataSourceURL,
            dataSource,
            eventuateConfigurationProperties.getBinlogClientId(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            curatorFramework,
            eventuateConfigurationProperties.getLeadershipLockPath(),
            offsetStore,
            debeziumBinlogOffsetKafkaStore);
  }
}
