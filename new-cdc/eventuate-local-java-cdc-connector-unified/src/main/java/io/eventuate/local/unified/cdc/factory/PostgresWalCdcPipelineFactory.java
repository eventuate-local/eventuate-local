package io.eventuate.local.unified.cdc.factory;

import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
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

public class PostgresWalCdcPipelineFactory extends CommonDBLogCdcPipelineFactory<PostgresWalCdcPipelineProperties> {

  public PostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                       PublishingStrategy<PublishedEvent> publishingStrategy,
                                       DataProducerFactory dataProducerFactory,
                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                       EventuateKafkaProducer eventuateKafkaProducer) {
    super(curatorFramework,
            publishingStrategy,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer);
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
  public CdcPipeline create(PostgresWalCdcPipelineProperties cdcPipelineProperties) {
    OffsetStore offsetStore = createOffsetStore(cdcPipelineProperties);

    CdcDataPublisher<PublishedEvent> cdcDataPublisher = createCdcDataPublisher(offsetStore);

    PostgresWalMessageParser<PublishedEvent> publishedEventPostgresWalMessageParser = createPostgresReplicationMessageParser();

    DbLogClient<PublishedEvent> dbLogClient = createDbLogClient(cdcPipelineProperties, publishedEventPostgresWalMessageParser);

    CdcProcessor<PublishedEvent> cdcProcessor = createCdcProcessor(dbLogClient, offsetStore);

    EventTableChangesToAggregateTopicTranslator<PublishedEvent> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcPipelineProperties, cdcDataPublisher, cdcProcessor);

    return new CdcPipeline(publishedEventEventTableChangesToAggregateTopicTranslator);
  }

  private DbLogClient<PublishedEvent> createDbLogClient(PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties,
                                                 PostgresWalMessageParser<PublishedEvent> postgresWalMessageParser) {

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

  private PostgresWalMessageParser<PublishedEvent> createPostgresReplicationMessageParser() {
    return new PostgresWalJsonMessageParser();
  }

  private CdcProcessor<PublishedEvent> createCdcProcessor(DbLogClient<PublishedEvent> dbLogClient,
                                                   OffsetStore offsetStore) {

    return new DbLogBasedCdcProcessor<>(dbLogClient, offsetStore);
  }
}
