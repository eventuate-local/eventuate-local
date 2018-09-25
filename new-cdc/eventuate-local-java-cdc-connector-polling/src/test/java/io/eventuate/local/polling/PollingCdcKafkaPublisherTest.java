package io.eventuate.local.polling;

import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PollingIntegrationTestConfiguration.class)
public class PollingCdcKafkaPublisherTest extends CdcKafkaPublisherTest {

  @Autowired
  private PollingDao pollingDao;

  @Autowired
  private PollingDataProvider pollingDataProvider;

  @Before
  public void init() {
    super.init();

    pollingDao.addPollingEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            pollingDataProvider,
            new BinlogEntryToPublishedEventConverter(),
            cdcDataPublisher);

    pollingDao.start();
  }

  @Override
  protected CdcDataPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new PollingCdcDataPublisher<>(() ->
            new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
                    EventuateKafkaProducerConfigurationProperties.empty()),
            publishingStrategy);
  }

  @Override
  public void clear() {
    pollingDao.stop();
  }
}
