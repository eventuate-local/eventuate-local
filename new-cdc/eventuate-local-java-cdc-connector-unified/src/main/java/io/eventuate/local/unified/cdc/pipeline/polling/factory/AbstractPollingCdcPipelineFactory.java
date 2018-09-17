package io.eventuate.local.unified.cdc.pipeline.polling.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.PollingCdcDataPublisher;
import io.eventuate.local.polling.PollingCdcProcessor;
import io.eventuate.local.polling.PollingDao;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;
import java.util.function.Consumer;

public abstract class AbstractPollingCdcPipelineFactory<EVENT extends BinLogEvent> extends CommonCdcPipelineFactory<PollingPipelineProperties, EVENT> {

  public AbstractPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                           DataProducerFactory dataProducerFactory,
                                           BinlogEntryReaderProvider binlogEntryReaderProvider) {

    super(curatorFramework, dataProducerFactory, binlogEntryReaderProvider);
  }

  @Override
  public Class<PollingPipelineProperties> propertyClass() {
    return PollingPipelineProperties.class;
  }

  @Override
  public CdcPipeline<EVENT> create(PollingPipelineProperties cdcPipelineProperties) {
    EventuateSchema eventuateSchema = createEventuateSchema(cdcPipelineProperties);

    PollingDataProvider pollingDataProvider = createPollingDataProvider();

    DataSource dataSource = createDataSource(cdcPipelineProperties);

    PollingDao pollingDao =
            binlogEntryReaderProvider.getOrCreateClient(cdcPipelineProperties.getDataSourceUrl(),
                    cdcPipelineProperties.getDataSourceUserName(),
                    cdcPipelineProperties.getDataSourcePassword(),
                    () -> createPollingDao(cdcPipelineProperties, dataSource),
                    c -> {},
                    c -> {});

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(dataProducerFactory, createPublishingStrategy());

    CdcProcessor<EVENT> cdcProcessor = new PollingCdcProcessor<>(pollingDao,
            cdcPipelineProperties.getPollingIntervalInMilliseconds(),
            pollingDataProvider,
            createBinlogEntryToEventConverter(),
            cdcPipelineProperties.getDataSourceUrl(),
            eventuateSchema,
            createSourceTableNameSupplier(cdcPipelineProperties).getSourceTableName());

    EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcPipelineProperties, cdcDataPublisher, cdcProcessor);

    return new CdcPipeline<>(publishedEventEventTableChangesToAggregateTopicTranslator);
  }

  protected CdcDataPublisher<EVENT> createCdcDataPublisher(DataProducerFactory dataProducerFactory,
                                                           PublishingStrategy<EVENT> publishingStrategy) {

    return new PollingCdcDataPublisher<>(dataProducerFactory, publishingStrategy);
  }

  protected abstract PollingDataProvider createPollingDataProvider();

  public PollingDao createPollingDao(PollingPipelineProperties pollingPipelineProperties, DataSource dataSource) {

    return new PollingDao(dataSource,
            pollingPipelineProperties.getMaxEventsPerPolling(),
            pollingPipelineProperties.getMaxAttemptsForPolling(),
            pollingPipelineProperties.getPollingRetryIntervalInMilliseconds());
  }
}
