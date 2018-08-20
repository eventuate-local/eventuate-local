package io.eventuate.local.unified.cdc.pipeline.dblog.common.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.db.log.common.DbLogBasedCdcDataPublisher;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.CommonDbLogCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public abstract class CommonDBLogCdcPipelineFactory<PROPERTIES extends CommonDbLogCdcPipelineProperties, EVENT extends BinLogEvent>
        extends CommonCdcPipelineFactory<PROPERTIES, EVENT> {

  protected EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;
  protected EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;
  protected EventuateKafkaProducer eventuateKafkaProducer;
  protected PublishingFilter publishingFilter;

  public CommonDBLogCdcPipelineFactory(CuratorFramework curatorFramework,
                                       DataProducerFactory dataProducerFactory,
                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                       EventuateKafkaProducer eventuateKafkaProducer,
                                       PublishingFilter publishingFilter) {

    super(curatorFramework, dataProducerFactory);
    this.eventuateKafkaConfigurationProperties = eventuateKafkaConfigurationProperties;
    this.eventuateKafkaConsumerConfigurationProperties = eventuateKafkaConsumerConfigurationProperties;
    this.eventuateKafkaProducer = eventuateKafkaProducer;
    this.publishingFilter = publishingFilter;
  }

  protected abstract OffsetStore createOffsetStore(PROPERTIES properties,
                                                   DataSource dataSource,
                                                   EventuateSchema eventuateSchema);

  protected CdcDataPublisher<EVENT> createCdcDataPublisher(OffsetStore offsetStore) {

    return new DbLogBasedCdcDataPublisher<>(dataProducerFactory,
            offsetStore,
            publishingFilter,
            createPublishingStrategy());
  }
}
