package io.eventuate.local.polling;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
    return new PollingCdcProcessor<>(pollingDao,
            500,
            pollingDataProvider,
            new BinlogEntryToPublishedEventConverter(),
            dataSourceUrl,
            eventuateSchema,
            sourceTableNameSupplier.getSourceTableName());
  }
}
