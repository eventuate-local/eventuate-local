package io.eventuate.local.unified.cdc.pipeline.polling.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.PollingCdcDataPublisher;
import io.eventuate.local.polling.PollingCdcProcessor;
import io.eventuate.local.polling.PollingDao;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public abstract class AbstractPollingCdcPipelineFactory<EVENT extends BinLogEvent, EVENT_BEAN, ID> extends CommonCdcPipelineFactory<PollingPipelineProperties, EVENT> {

  public AbstractPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                           DataProducerFactory dataProducerFactory) {

    super(curatorFramework, dataProducerFactory);
  }

  @Override
  public Class<PollingPipelineProperties> propertyClass() {
    return PollingPipelineProperties.class;
  }

  @Override
  public CdcPipeline<EVENT> create(PollingPipelineProperties cdcPipelineProperties) {
    EventuateSchema eventuateSchema = createEventuateSchema(cdcPipelineProperties);

    PollingDataProvider<EVENT_BEAN, EVENT, ID> pollingDataProvider = createPollingDataProvider(eventuateSchema);

    DataSource dataSource = createDataSource(cdcPipelineProperties);

    PollingDao<EVENT_BEAN, EVENT, ID> pollingDao = createPollingDao(cdcPipelineProperties, pollingDataProvider, dataSource);

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(dataProducerFactory, createPublishingStrategy());

    CdcProcessor<EVENT> cdcProcessor = createCdcProcessor(cdcPipelineProperties, pollingDao);

    EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcPipelineProperties, cdcDataPublisher, cdcProcessor);

    return new CdcPipeline<>(publishedEventEventTableChangesToAggregateTopicTranslator);
  }

  protected CdcDataPublisher<EVENT> createCdcDataPublisher(DataProducerFactory dataProducerFactory,
                                                           PublishingStrategy<EVENT> publishingStrategy) {

    return new PollingCdcDataPublisher<>(dataProducerFactory, publishingStrategy);
  }

  protected abstract PollingDataProvider<EVENT_BEAN, EVENT, ID> createPollingDataProvider(EventuateSchema eventuateSchema);

  public PollingDao<EVENT_BEAN, EVENT, ID> createPollingDao(PollingPipelineProperties pollingPipelineProperties,
                                                                                 PollingDataProvider<EVENT_BEAN, EVENT, ID> pollingDataProvider,
                                                                                 DataSource dataSource) {

    return new PollingDao<>(pollingDataProvider,
            dataSource,
            pollingPipelineProperties.getMaxEventsPerPolling(),
            pollingPipelineProperties.getMaxAttemptsForPolling(),
            pollingPipelineProperties.getPollingRetryIntervalInMilliseconds());
  }

  protected CdcProcessor<EVENT> createCdcProcessor(PollingPipelineProperties pollingPipelineProperties,
                                                   PollingDao<EVENT_BEAN, EVENT, ID> pollingDao) {

    return new PollingCdcProcessor<>(pollingDao, pollingPipelineProperties.getPollingIntervalInMilliseconds());
  }
}
