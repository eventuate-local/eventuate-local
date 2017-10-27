package io.eventuate.local.polling;

import io.eventuate.local.common.CdcKafkaPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PollingIntegrationTestConfiguration.class)
@IntegrationTest
public class PollingCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Override
  protected CdcKafkaPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new PollingCdcKafkaPublisher<>(eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }
}
