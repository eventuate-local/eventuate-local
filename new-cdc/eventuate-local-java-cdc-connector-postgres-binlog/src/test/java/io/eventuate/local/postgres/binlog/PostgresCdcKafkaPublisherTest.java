package io.eventuate.local.postgres.binlog;

import io.eventuate.local.common.CdcKafkaPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ActiveProfiles("PostgresWal")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PostgresBinlogCdcIntegrationTestConfiguration.class)
@IntegrationTest
public class PostgresCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Autowired
  private DatabaseLastSequenceNumberKafkaStore databaseLastSequenceNumberKafkaStore;

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Override
  protected CdcKafkaPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new PostgresCdcKafkaPublisher<>(databaseLastSequenceNumberKafkaStore,
            eventuateKafkaConfigurationProperties.getBootstrapServers(),
            publishingStrategy);
  }
}
