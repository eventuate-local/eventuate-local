package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.test.util.CdcProcessorTest;
import io.eventuate.local.test.util.SourceTableNameSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Consumer;

public abstract class AbstractMySQLCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  protected MySqlBinaryLogClient mySqlBinaryLogClient;

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Override
  protected void prepareBinlogEntryHandler(Consumer<PublishedEvent> consumer) {
    mySqlBinaryLogClient.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            new PublishedEventPublishingStrategy());

    mySqlBinaryLogClient.setCdcDataPublisherFactory(dataProducer -> new CdcDataPublisher<PublishedEvent>(null, null) {
      @Override
      public void handleEvent(PublishedEvent publishedEvent, PublishingStrategy<PublishedEvent> publishingStrategy) throws EventuateLocalPublishingException {
        consumer.accept(publishedEvent);
      }
    });
  }

  @Override
  protected void startEventProcessing() {
    mySqlBinaryLogClient.start();
  }

  @Override
  protected void stopEventProcessing() {
    mySqlBinaryLogClient.stop();
  }
}
