package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
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

    EventuateSchema eventuateSchema = createEventuateSchema(cdcPipelineProperties);

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(mySqlBinaryLogClient.getOffsetStore());

    SourceTableNameSupplier sourceTableNameSupplier = createSourceTableNameSupplier(cdcPipelineProperties);

    BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter = createBinlogEntryToEventConverter();

    CdcProcessor<EVENT> cdcProcessor = createCdcProcessor(mySqlBinaryLogClient,
            binlogEntryToEventConverter,
            sourceTableNameSupplier,
            eventuateSchema);

    EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcDataPublisher, cdcProcessor);

    return new CdcPipeline<>(publishedEventEventTableChangesToAggregateTopicTranslator);
  }
}
