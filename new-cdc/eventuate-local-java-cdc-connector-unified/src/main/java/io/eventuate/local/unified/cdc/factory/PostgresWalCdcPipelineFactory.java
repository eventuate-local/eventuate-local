package io.eventuate.local.unified.cdc.factory;

import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.DuplicatePublishingDetector;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.postgres.wal.PostgresWalJsonMessageParser;
import io.eventuate.local.postgres.wal.PostgresWalMessageParser;
import io.eventuate.local.unified.cdc.properties.PostgresWalCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

public class PostgresWalCdcPipelineFactory extends AbstractPostgresWalCdcPipelineFactory<PublishedEvent> {

  public PostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                       PublishingStrategy<PublishedEvent> publishingStrategy,
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
  protected PostgresWalMessageParser<PublishedEvent> createPostgresReplicationMessageParser() {
    return new PostgresWalJsonMessageParser();
  }

  @Override
  protected OffsetStore createOffsetStore(PostgresWalCdcPipelineProperties properties) {

    return new DatabaseOffsetKafkaStore(properties.getDbHistoryTopicName(),
            properties.getMySqlBinLogClientName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }
}
