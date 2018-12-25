package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.db.log.common.OffsetStore;
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

  @Autowired
  private OffsetStore offsetStore;

  @Override
  protected void prepareBinlogEntryHandler(Consumer<PublishedEvent> consumer) {
    mySqlBinaryLogClient.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            new CdcDataPublisher<PublishedEvent>(null, null, null, null) {
              @Override
              public void handleEvent(PublishedEvent publishedEvent) throws EventuateLocalPublishingException {
                consumer.accept(publishedEvent);
              }
            });
  }

  @Override
  public void onEventSent(PublishedEvent publishedEvent) {
    offsetStore.save(publishedEvent.getBinlogFileOffset().get());
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
