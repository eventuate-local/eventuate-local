package io.eventuate.local.unified.cdc.factory;

import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.postgres.wal.PostgresWalJsonMessageParser;
import io.eventuate.local.postgres.wal.PostgresWalMessageParser;
import io.eventuate.local.unified.cdc.CdcPipelineType;
import io.eventuate.local.unified.cdc.pipeline.CdcPipeline;
import io.eventuate.local.unified.cdc.properties.PostgresWalCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

public abstract class AbstractPostgresWalCdcPipelineFactory<EVENT extends BinLogEvent> extends CommonDBLogCdcPipelineFactory<PostgresWalCdcPipelineProperties, EVENT> {

  public AbstractPostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                               PublishingStrategy<EVENT> publishingStrategy,
                                               DataProducerFactory dataProducerFactory,
                                               EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                               EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                               EventuateKafkaProducer eventuateKafkaProducer,
                                               PublishingFilter publishingFilter) {
    super(curatorFramework,
            publishingStrategy,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter);
  }

  @Override
  public boolean supports(String type) {
    return CdcPipelineType.POSTGRES_WAL.stringRepresentation.equals(type);
  }

  @Override
  public Class<PostgresWalCdcPipelineProperties> propertyClass() {
    return PostgresWalCdcPipelineProperties.class;
  }

  @Override
  public CdcPipeline<EVENT> create(PostgresWalCdcPipelineProperties cdcPipelineProperties) {
    OffsetStore offsetStore = createOffsetStore(cdcPipelineProperties);

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(offsetStore);

    PostgresWalMessageParser<EVENT> publishedEventPostgresWalMessageParser = createPostgresReplicationMessageParser();

    DbLogClient<EVENT> dbLogClient = createDbLogClient(cdcPipelineProperties, publishedEventPostgresWalMessageParser);

    CdcProcessor<EVENT> cdcProcessor = createCdcProcessor(dbLogClient, offsetStore);

    EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcPipelineProperties, cdcDataPublisher, cdcProcessor);

    return new CdcPipeline<>(publishedEventEventTableChangesToAggregateTopicTranslator);
  }

  protected DbLogClient<EVENT> createDbLogClient(PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties,
                                                        PostgresWalMessageParser<EVENT> postgresWalMessageParser) {

    return new PostgresWalClient<>(postgresWalMessageParser,
            postgresWalCdcPipelineProperties.getDataSourceUrl(),
            postgresWalCdcPipelineProperties.getDataSourceUserName(),
            postgresWalCdcPipelineProperties.getDataSourcePassword(),
            postgresWalCdcPipelineProperties.getBinlogConnectionTimeoutInMilliseconds(),
            postgresWalCdcPipelineProperties.getMaxAttemptsForBinlogConnection(),
            postgresWalCdcPipelineProperties.getPostgresWalIntervalInMilliseconds(),
            postgresWalCdcPipelineProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            postgresWalCdcPipelineProperties.getPostgresReplicationSlotName());
  }

  protected abstract PostgresWalMessageParser<EVENT> createPostgresReplicationMessageParser();

  protected CdcProcessor<EVENT> createCdcProcessor(DbLogClient<EVENT> dbLogClient,
                                                          OffsetStore offsetStore) {

    return new DbLogBasedCdcProcessor<>(dbLogClient, offsetStore);
  }
}
