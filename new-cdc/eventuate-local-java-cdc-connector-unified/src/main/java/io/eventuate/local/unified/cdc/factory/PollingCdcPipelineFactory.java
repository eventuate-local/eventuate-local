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

public class PollingCdcPipelineFactory extends CommonCdcPipelineFactory<PollingPipelineProperties> {

  public PollingCdcPipelineFactory(CuratorFramework curatorFramework, PublishingStrategy<PublishedEvent> publishingStrategy,
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
  public CdcPipeline create(PollingPipelineProperties cdcPipelineProperties) {
    EventuateSchema eventuateSchema = createEventuateSchema(cdcPipelineProperties);

    CdcDataPublisher<PublishedEvent> cdcDataPublisher = createCdcDataPublisher(dataProducerFactory, publishingStrategy);

    PollingDataProvider<PublishedEventBean, PublishedEvent, String> pollingDataProvider = createPollingDataProvider(eventuateSchema);

    DataSource dataSource = createDataSource(cdcPipelineProperties);

    PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao = createPollingDao(cdcPipelineProperties, pollingDataProvider, dataSource);

    CdcProcessor<PublishedEvent> cdcProcessor = createCdcProcessor(cdcPipelineProperties, pollingDao);

    EventTableChangesToAggregateTopicTranslator<PublishedEvent> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcPipelineProperties, cdcDataPublisher, cdcProcessor);

    return new CdcPipeline(publishedEventEventTableChangesToAggregateTopicTranslator);
  }

  private CdcDataPublisher<PublishedEvent> createCdcDataPublisher(DataProducerFactory dataProducerFactory,
                                                                  PublishingStrategy<PublishedEvent> publishingStrategy) {
    return new PollingCdcDataPublisher<>(dataProducerFactory, publishingStrategy);
  }

  private PollingDataProvider<PublishedEventBean, PublishedEvent, String> createPollingDataProvider(EventuateSchema eventuateSchema) {
    return new EventPollingDataProvider(eventuateSchema);
  }

  public PollingDao<PublishedEventBean, PublishedEvent, String> createPollingDao(PollingPipelineProperties pollingPipelineProperties,
                                                                                 PollingDataProvider<PublishedEventBean, PublishedEvent, String> pollingDataProvider,
                                                                                 DataSource dataSource) {

    return new PollingDao<>(pollingDataProvider,
            dataSource,
            pollingPipelineProperties.getMaxEventsPerPolling(),
            pollingPipelineProperties.getMaxAttemptsForPolling(),
            pollingPipelineProperties.getPollingRetryIntervalInMilliseconds());
  }

  private CdcProcessor<PublishedEvent> createCdcProcessor(PollingPipelineProperties pollingPipelineProperties,
                                                          PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao) {
    return new PollingCdcProcessor<>(pollingDao, pollingPipelineProperties.getPollingIntervalInMilliseconds());
  }

  private DataSource createDataSource(PollingPipelineProperties pollingPipelineProperties) {
    return DataSourceBuilder
            .create()
            .username(pollingPipelineProperties.getDataSourceUserName())
            .password(pollingPipelineProperties.getDataSourcePassword())
            .url(pollingPipelineProperties.getDataSourceUrl())
            .driverClassName(pollingPipelineProperties.getDataSourceDriverClassName())
            .build();
  }
}
