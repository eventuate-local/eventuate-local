package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishedEventPublishingStrategy;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.postgres.wal.PostgresWalJsonMessageParser;
import io.eventuate.local.postgres.wal.PostgresWalMessageParser;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public class PostgresWalCdcPipelineFactory extends AbstractPostgresWalCdcPipelineFactory<PublishedEvent> {

  public static final String TYPE = "eventuate-local-postgres-wal";

  public PostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
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
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  protected PostgresWalMessageParser<PublishedEvent> createPostgresReplicationMessageParser() {
    return new PostgresWalJsonMessageParser();
  }

  @Override
  protected OffsetStore createOffsetStore(PostgresWalCdcPipelineProperties properties,
                                          DataSource dataSource,
                                          EventuateSchema eventuateSchema) {

    return new DatabaseOffsetKafkaStore(properties.getDbHistoryTopicName(),
            properties.getMySqlBinLogClientName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  @Override
  protected PublishingStrategy<PublishedEvent> createPublishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }
}
