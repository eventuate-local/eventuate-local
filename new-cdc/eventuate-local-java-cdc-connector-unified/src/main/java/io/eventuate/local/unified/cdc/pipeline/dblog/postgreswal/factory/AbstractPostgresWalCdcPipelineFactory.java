package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
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
                                               PublishingFilter publishingFilter) {
    super(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter);
  }

  @Override
  public Class<PostgresWalCdcPipelineProperties> propertyClass() {
    return PostgresWalCdcPipelineProperties.class;
  }

  @Override
  public CdcPipeline<EVENT> create(PostgresWalCdcPipelineProperties cdcPipelineProperties) {
    DataSource dataSource = createDataSource(cdcPipelineProperties);

    EventuateSchema eventuateSchema = createEventuateSchema(cdcPipelineProperties);

    OffsetStore offsetStore = createOffsetStore(cdcPipelineProperties, dataSource, eventuateSchema);

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(offsetStore);

    SourceTableNameSupplier sourceTableNameSupplier = createSourceTableNameSupplier(cdcPipelineProperties);

    DbLogClient dbLogClient = createDbLogClient(sourceTableNameSupplier, cdcPipelineProperties);

    BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter = createBinlogEntryToEventConverter();

    CdcProcessor<EVENT> cdcProcessor = createCdcProcessor(dbLogClient, offsetStore, binlogEntryToEventConverter);

    EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcPipelineProperties, cdcDataPublisher, cdcProcessor);

    return new CdcPipeline<>(publishedEventEventTableChangesToAggregateTopicTranslator);
  }

  protected DbLogClient createDbLogClient(SourceTableNameSupplier sourceTableNameSupplier,
                                          PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties) {

    return new PostgresWalClient(sourceTableNameSupplier.getSourceTableName(),
            postgresWalCdcPipelineProperties.getDataSourceUrl(),
            postgresWalCdcPipelineProperties.getDataSourceUserName(),
            postgresWalCdcPipelineProperties.getDataSourcePassword(),
            postgresWalCdcPipelineProperties.getBinlogConnectionTimeoutInMilliseconds(),
            postgresWalCdcPipelineProperties.getMaxAttemptsForBinlogConnection(),
            postgresWalCdcPipelineProperties.getPostgresWalIntervalInMilliseconds(),
            postgresWalCdcPipelineProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            postgresWalCdcPipelineProperties.getPostgresReplicationSlotName());
  }

  protected CdcProcessor<EVENT> createCdcProcessor(DbLogClient dbLogClient,
                                                   OffsetStore offsetStore,
                                                   BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter) {

    return new DbLogBasedCdcProcessor<>(dbLogClient, offsetStore, binlogEntryToEventConverter);
  }
}
