package io.eventuate.local.unified.cdc.factory;

import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.properties.CommonDbLogCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

abstract public class CommonDBLogCdcPipelineFactory<PROPERTIES extends CommonDbLogCdcPipelineProperties> extends CommonCdcPipelineFactory<PROPERTIES> {
  protected DataProducerFactory dataProducerFactory;
  protected EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;
  protected EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;
  protected EventuateKafkaProducer eventuateKafkaProducer;
  protected PublishingStrategy<PublishedEvent> publishingStrategy;

  public CommonDBLogCdcPipelineFactory(CuratorFramework curatorFramework,
                                       DataProducerFactory dataProducerFactory,
                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                       EventuateKafkaProducer eventuateKafkaProducer,
                                       PublishingStrategy<PublishedEvent> publishingStrategy) {
    super(curatorFramework);
    this.dataProducerFactory = dataProducerFactory;
    this.eventuateKafkaConfigurationProperties = eventuateKafkaConfigurationProperties;
    this.eventuateKafkaConsumerConfigurationProperties = eventuateKafkaConsumerConfigurationProperties;
    this.eventuateKafkaProducer = eventuateKafkaProducer;
    this.publishingStrategy = publishingStrategy;
  }

  protected OffsetStore createOffsetStore(PROPERTIES properties) {

    return new DatabaseOffsetKafkaStore(properties.getDbHistoryTopicName(),
            properties.getMySqlBinLogClientName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  protected CdcDataPublisher<PublishedEvent> createCdcDataPublisher(OffsetStore offsetStore) {

    return new DbLogBasedCdcDataPublisher<>(dataProducerFactory,
            offsetStore,
            new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(), eventuateKafkaConsumerConfigurationProperties),
            publishingStrategy);
  }
}
