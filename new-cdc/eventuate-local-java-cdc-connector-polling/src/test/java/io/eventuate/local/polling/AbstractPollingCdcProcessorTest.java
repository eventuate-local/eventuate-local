package io.eventuate.local.polling;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.test.util.SourceTableNameSupplier;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Consumer;

public abstract class AbstractPollingCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  protected PollingDao pollingDao;

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Override
  protected void prepareBinlogEntryHandler(Consumer<PublishedEvent> consumer) {
    pollingDao.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            new PublishedEventPublishingStrategy());

    pollingDao.setCdcDataPublisherFactory(dataProducer -> new CdcDataPublisher<PublishedEvent>(null, null) {
      @Override
      public void handleEvent(PublishedEvent publishedEvent, PublishingStrategy<PublishedEvent> publishingStrategy) throws EventuateLocalPublishingException {
        consumer.accept(publishedEvent);
      }
    });
  }

  @Override
  protected void startEventProcessing() {
    pollingDao.start();
  }

  @Override
  protected void stopEventProcessing() {
    pollingDao.stop();
  }
}
