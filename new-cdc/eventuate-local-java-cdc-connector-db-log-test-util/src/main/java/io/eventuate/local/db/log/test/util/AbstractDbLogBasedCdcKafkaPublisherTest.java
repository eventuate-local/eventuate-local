package io.eventuate.local.db.log.test.util;

import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.DbLogBasedCdcDataPublisher;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;


public class AbstractDbLogBasedCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Autowired
  private DatabaseOffsetKafkaStore databaseOffsetKafkaStore;

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Override
  protected CdcDataPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new DbLogBasedCdcDataPublisher<>(() ->
            new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers()),
            databaseOffsetKafkaStore,
            eventuateKafkaConfigurationProperties.getBootstrapServers(),
            publishingStrategy);
  }
}
