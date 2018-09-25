package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.MySqlBinaryLogClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.CommonDBLogCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;

public abstract class AbstractMySqlBinlogCdcPipelineFactory<EVENT extends BinLogEvent> extends CommonDBLogCdcPipelineFactory< EVENT> {

  public AbstractMySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
                                               DataProducerFactory dataProducerFactory,
                                               EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                               EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                               EventuateKafkaProducer eventuateKafkaProducer,
                                               PublishingFilter publishingFilter,
                                               BinlogEntryReaderProvider binlogEntryReaderProvider) {
    super(curatorFramework,
          dataProducerFactory,
          eventuateKafkaConfigurationProperties,
          eventuateKafkaConsumerConfigurationProperties,
          eventuateKafkaProducer,
          publishingFilter,
          binlogEntryReaderProvider);
  }

  @Override
  public CdcPipeline<EVENT> create(CdcPipelineProperties cdcPipelineProperties) {
    MySqlBinaryLogClient mySqlBinaryLogClient = binlogEntryReaderProvider.getReader(cdcPipelineProperties.getReader());

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(mySqlBinaryLogClient.getOffsetStore());

    mySqlBinaryLogClient.addBinlogEntryHandler(createEventuateSchema(cdcPipelineProperties),
            createSourceTableNameSupplier(cdcPipelineProperties).getSourceTableName(),
            createBinlogEntryToEventConverter(),
            cdcDataPublisher);

    return new CdcPipeline<>(cdcDataPublisher);
  }
}
