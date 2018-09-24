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
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

public abstract class AbstractPollingCdcPipelineFactory<EVENT extends BinLogEvent> extends CommonCdcPipelineFactory<EVENT> {

  public AbstractPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                           DataProducerFactory dataProducerFactory,
                                           BinlogEntryReaderProvider binlogEntryReaderProvider) {

    super(curatorFramework, dataProducerFactory, binlogEntryReaderProvider);
  }

  @Override
  public CdcPipeline<EVENT> create(CdcPipelineProperties cdcPipelineProperties) {
    EventuateSchema eventuateSchema = createEventuateSchema(cdcPipelineProperties);

    PollingDataProvider pollingDataProvider = createPollingDataProvider();

    PollingDao pollingDao = binlogEntryReaderProvider.getReader(cdcPipelineProperties.getReader());

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(dataProducerFactory, createPublishingStrategy());

    CdcProcessor<EVENT> cdcProcessor = new PollingCdcProcessor<>(pollingDao,
            pollingDataProvider,
            createBinlogEntryToEventConverter(),
            eventuateSchema,
            createSourceTableNameSupplier(cdcPipelineProperties).getSourceTableName());

    EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcDataPublisher, cdcProcessor);

    return new CdcPipeline<>(publishedEventEventTableChangesToAggregateTopicTranslator);
  }

  protected CdcDataPublisher<EVENT> createCdcDataPublisher(DataProducerFactory dataProducerFactory,
                                                           PublishingStrategy<EVENT> publishingStrategy) {

    return new PollingCdcDataPublisher<>(dataProducerFactory, publishingStrategy);
  }

  protected abstract PollingDataProvider createPollingDataProvider();
}
