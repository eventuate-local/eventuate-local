package io.eventuate.local.polling;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Consumer;

public abstract class AbstractPollingCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private PollingDao pollingDao;

  @Autowired
  private PollingDataProvider pollingDataProvider;

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new PollingCdcProcessor<PublishedEvent>(pollingDao,
            pollingDataProvider,
            new BinlogEntryToPublishedEventConverter(),
            eventuateSchema,
            sourceTableNameSupplier.getSourceTableName()) {
      @Override
      public void start(Consumer consumer) {
        super.start(consumer);
        pollingDao.start();
      }

      @Override
      public void stop() {
        pollingDao.stop();
        super.stop();
      }
    };
  }
}
