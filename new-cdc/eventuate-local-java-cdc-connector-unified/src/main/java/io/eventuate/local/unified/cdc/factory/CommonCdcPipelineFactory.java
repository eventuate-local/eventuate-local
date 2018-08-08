package io.eventuate.local.unified.cdc.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.properties.CdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

abstract public class CommonCdcPipelineFactory<PROPERTIES extends CdcPipelineProperties> implements CdcPipelineFactory<PROPERTIES> {
  protected CuratorFramework curatorFramework;
  protected PublishingStrategy<PublishedEvent> publishingStrategy;
  protected DataProducerFactory dataProducerFactory;

  public CommonCdcPipelineFactory(CuratorFramework curatorFramework, PublishingStrategy<PublishedEvent> publishingStrategy, DataProducerFactory dataProducerFactory) {
    this.curatorFramework = curatorFramework;
    this.publishingStrategy = publishingStrategy;
    this.dataProducerFactory = dataProducerFactory;
  }

  protected EventTableChangesToAggregateTopicTranslator<PublishedEvent> createEventTableChangesToAggregateTopicTranslator(PROPERTIES properties,
                                                                                                                          CdcDataPublisher<PublishedEvent> cdcDataPublisher,
                                                                                                                          CdcProcessor<PublishedEvent> cdcProcessor) {


    return new EventTableChangesToAggregateTopicTranslator<>(cdcDataPublisher,
            cdcProcessor,
            curatorFramework,
            properties.getLeadershipLockPath());
  }

  protected EventuateSchema createEventuateSchema(PROPERTIES properties) {
    return new EventuateSchema(properties.getEventuateDatabaseSchema());
  }
}
