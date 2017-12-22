package io.eventuate.local.db.log.test.util;

import io.eventuate.local.common.CdcKafkaPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.ReplicationLogBasedCdcKafkaPublisher;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;


public class AbstractReplicationLogBasedCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Autowired
  private DatabaseOffsetKafkaStore databaseOffsetKafkaStore;

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Override
  protected CdcKafkaPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new ReplicationLogBasedCdcKafkaPublisher<>(databaseOffsetKafkaStore,
            eventuateKafkaConfigurationProperties.getBootstrapServers(),
            publishingStrategy);
  }
}
