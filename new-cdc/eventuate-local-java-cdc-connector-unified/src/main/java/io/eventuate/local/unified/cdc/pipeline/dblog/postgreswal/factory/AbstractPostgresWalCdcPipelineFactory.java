package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.postgres.wal.PostgresWalCdcProcessor;
import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.CommonDBLogCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public abstract class AbstractPostgresWalCdcPipelineFactory<EVENT extends BinLogEvent> extends CommonDBLogCdcPipelineFactory<PostgresWalCdcPipelineProperties, EVENT> {

  public AbstractPostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
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
  public Class<PostgresWalCdcPipelineProperties> propertyClass() {
    return PostgresWalCdcPipelineProperties.class;
  }

  @Override
  public CdcPipeline<EVENT> create(PostgresWalCdcPipelineProperties cdcPipelineProperties) {
    PostgresWalClient postgresWalClient = binlogEntryReaderProvider.getReader(cdcPipelineProperties.getReader());

    EventuateSchema eventuateSchema = createEventuateSchema(cdcPipelineProperties);

    OffsetStore offsetStore = createOffsetStore(cdcPipelineProperties,
            postgresWalClient.getDataSource(),
            eventuateSchema,
            postgresWalClient.getName());

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(offsetStore);

    SourceTableNameSupplier sourceTableNameSupplier = createSourceTableNameSupplier(cdcPipelineProperties);

    BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter = createBinlogEntryToEventConverter();

    CdcProcessor<EVENT> cdcProcessor = new PostgresWalCdcProcessor<>(postgresWalClient,
            offsetStore,
            binlogEntryToEventConverter,
            sourceTableNameSupplier.getSourceTableName(),
            eventuateSchema);

    EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcDataPublisher, cdcProcessor);

    return new CdcPipeline<>(publishedEventEventTableChangesToAggregateTopicTranslator);
  }
}
