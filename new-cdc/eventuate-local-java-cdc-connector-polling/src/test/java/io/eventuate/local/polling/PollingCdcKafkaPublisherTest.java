package io.eventuate.local.polling;

import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.DuplicatePublishingDetector;
import io.eventuate.local.common.PublishedEvent;
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

  @Autowired
  private DuplicatePublishingDetector duplicatePublishingDetector;

  @Before
  public void init() {
    super.init();

    pollingDao.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier,
            new BinlogEntryToPublishedEventConverter(),
            cdcDataPublisher);

    pollingDao.start();
  }

  @Override
  protected CdcDataPublisher<PublishedEvent> createCdcKafkaPublisher() {
    return new CdcDataPublisher<>(() ->
            new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
                    EventuateKafkaProducerConfigurationProperties.empty()),
            duplicatePublishingDetector,
            publishingStrategy);
  }

  @Override
  public void clear() {
    pollingDao.stop();
  }
}
