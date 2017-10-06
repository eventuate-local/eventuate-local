package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.mysql.binlog.exception.EventuateLocalPublishingException;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MySqlBinlogCdcIntegrationTestConfiguration.class)
@IntegrationTest
public class MySQLCdcKafkaPublisherTest extends AbstractCdcTest {

  @Autowired
  EventuateJdbcAccess eventuateJdbcAccess;

  @Autowired
  EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;

  @Autowired
  private PublishingStrategy<PublishedEvent> publishingStrategy;

  @Autowired
  DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;

  @Autowired
  private MySQLCdcProcessor<PublishedEvent> mySQLCdcProcessor;

  @Autowired
  private EventuateLocalAggregateCrud localAggregateCrud;

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Test
  public void shouldSendPublishedEventsToKafka() throws InterruptedException {
    MySQLCdcKafkaPublisher<PublishedEvent> mySQLCdcKafkaPublisher = new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore,
            eventuateKafkaConfigurationProperties.getBootstrapServers(),
            publishingStrategy);
    mySQLCdcKafkaPublisher.start();

    mySQLCdcProcessor.start(publishedEvent -> {
      try {
        mySQLCdcKafkaPublisher.handleEvent(publishedEvent);
      } catch (EventuateLocalPublishingException e) {
        throw new RuntimeException(e);
      }
    });

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);

    KafkaConsumer<String, String> consumer = createConsumer(eventuateKafkaConfigurationProperties.getBootstrapServers());
    consumer.partitionsFor(getEventTopicName());
    consumer.subscribe(Collections.singletonList(getEventTopicName()));

    waitForEventInKafka(consumer, entityIdVersionAndEventIds.getEntityId(), LocalDateTime.now().plusSeconds(20));
    mySQLCdcKafkaPublisher.stop();
  }

}
