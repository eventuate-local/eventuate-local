package io.eventuate.local.polling;

import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.junit.Before;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PollingIntegrationTestConfiguration.class)
public class PollingCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Before
  public void init() {
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Override
  protected CdcDataPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new PollingCdcDataPublisher<>(() ->
            new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers()),
            publishingStrategy);
  }
}
