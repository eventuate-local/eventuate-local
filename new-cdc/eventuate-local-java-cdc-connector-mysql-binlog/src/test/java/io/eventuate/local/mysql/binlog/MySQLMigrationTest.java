package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.commonimpl.EventTypeAndData;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
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
public class MySQLMigrationTest extends AbstractCdcTest {

  @Value("${spring.datasource.url}")
  private String dataSourceURL;

  @Autowired
  EventuateJdbcAccess eventuateJdbcAccess;

  @Autowired
  private OffsetStore offsetStore;

  @Autowired
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  @Autowired
  private MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient;

  @Test
  public void test() throws Exception {

    final String event = "TestEvent_MIGRATION";

    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();
    CdcProcessor<PublishedEvent> cdcProcessor = createMySQLCdcProcessor();
    cdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      offsetStore.save(publishedEvent.getBinlogFileOffset());
    });

    EventuateLocalAggregateCrud localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
    List<EventTypeAndData> events = Collections.singletonList(new EventTypeAndData(event, "{}", Optional.empty()));
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = localAggregateCrud.save("TestAggregate_MIGRATION", events, Optional.empty());

    PublishedEvent publishedEvent;

    while((publishedEvent = publishedEvents.poll(10, TimeUnit.SECONDS)) != null) {
      if (event.equals(publishedEvent.getEventType())) {
        break;
      }
    }

    Assert.assertNotNull(publishedEvent);
    Assert.assertEquals(event, publishedEvent.getEventType());
    Assert.assertEquals(entityIdVersionAndEventIds.getEntityVersion().asString(), publishedEvent.getId());
  }

  private CdcProcessor<PublishedEvent> createMySQLCdcProcessor() {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, offsetStore, debeziumBinlogOffsetKafkaStore);
  }
}
