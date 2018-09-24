package io.eventuate.local.unified.cdc.pipeline.dblog.common.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogEntryToEventConverter;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.db.log.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;

public abstract class CommonDBLogCdcPipelineFactory<EVENT extends BinLogEvent>
        extends CommonCdcPipelineFactory<EVENT> {

  protected EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;
  protected EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;
  protected EventuateKafkaProducer eventuateKafkaProducer;
  protected PublishingFilter publishingFilter;

  public CommonDBLogCdcPipelineFactory(CuratorFramework curatorFramework,
                                       DataProducerFactory dataProducerFactory,
                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                       EventuateKafkaProducer eventuateKafkaProducer,
                                       PublishingFilter publishingFilter,
                                       BinlogEntryReaderProvider binlogEntryReaderProvider) {

    super(curatorFramework, dataProducerFactory, binlogEntryReaderProvider);
    this.eventuateKafkaConfigurationProperties = eventuateKafkaConfigurationProperties;
    this.eventuateKafkaConsumerConfigurationProperties = eventuateKafkaConsumerConfigurationProperties;
    this.eventuateKafkaProducer = eventuateKafkaProducer;
    this.publishingFilter = publishingFilter;
  }

  protected CdcDataPublisher<EVENT> createCdcDataPublisher(OffsetStore offsetStore) {

    return new DbLogBasedCdcDataPublisher<>(dataProducerFactory,
            offsetStore,
            publishingFilter,
            createPublishingStrategy());
  }

  protected DbLogBasedCdcProcessor<EVENT> createCdcProcessor(DbLogClient dbLogClient,
                                                             BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                                             SourceTableNameSupplier sourceTableNameSupplier,
                                                             EventuateSchema eventuateSchema) {
    return new DbLogBasedCdcProcessor<EVENT>(dbLogClient,
            binlogEntryToEventConverter,
            sourceTableNameSupplier.getSourceTableName(),
            eventuateSchema) {
    };
  }
}
