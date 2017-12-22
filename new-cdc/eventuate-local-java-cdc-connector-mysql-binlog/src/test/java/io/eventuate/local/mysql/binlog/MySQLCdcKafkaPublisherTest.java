package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.CdcKafkaPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.ReplicationLogBasedCdcKafkaPublisher;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MySqlBinlogCdcIntegrationTestConfiguration.class)
@IntegrationTest
public class MySQLCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Autowired
  DatabaseOffsetKafkaStore binlogOffsetKafkaStore;

  @Override
  protected CdcKafkaPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new ReplicationLogBasedCdcKafkaPublisher<>(binlogOffsetKafkaStore,
            eventuateKafkaConfigurationProperties.getBootstrapServers(),
            publishingStrategy);
  }
}
