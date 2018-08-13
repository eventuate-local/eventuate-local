package io.eventuate.local.unified.cdc.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.*;
import io.eventuate.local.unified.cdc.CdcPipelineType;
import io.eventuate.local.unified.cdc.pipeline.CdcPipeline;
import io.eventuate.local.unified.cdc.properties.PollingPipelineProperties;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

public abstract class AbstractPollingCdcPipelineFactory<EVENT extends BinLogEvent, EVENT_BEAN, ID> extends CommonCdcPipelineFactory<PollingPipelineProperties, EVENT> {

  public AbstractPollingCdcPipelineFactory(CuratorFramework curatorFramework, PublishingStrategy<EVENT> publishingStrategy,
                                           DataProducerFactory dataProducerFactory) {

    super(curatorFramework, publishingStrategy, dataProducerFactory);
  }

  @Override
  public boolean supports(String type) {
    return CdcPipelineType.EVENT_POLLING.stringRepresentation.equals(type);
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

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(dataProducerFactory, publishingStrategy);

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
