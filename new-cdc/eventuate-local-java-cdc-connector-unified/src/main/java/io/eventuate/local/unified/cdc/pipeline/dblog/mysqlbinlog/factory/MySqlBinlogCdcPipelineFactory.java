package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public class MySqlBinlogCdcPipelineFactory extends AbstractMySqlBinlogCdcPipelineFactory<PublishedEvent> {
  public static final String TYPE = "eventuate-local";

  public MySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
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
  public boolean supports(String type, String readerType) {
    return TYPE.equals(type) && MySqlBinlogCdcPipelineReaderFactory.TYPE.equals(readerType);
  }

  @Override
  protected BinlogEntryToEventConverter<PublishedEvent> createBinlogEntryToEventConverter() {
    return new BinlogEntryToPublishedEventConverter();
  }

  @Override
  protected SourceTableNameSupplier createSourceTableNameSupplier(CdcPipelineProperties cdcPipelineProperties) {
    return new SourceTableNameSupplier(cdcPipelineProperties.getSourceTableName(), "events");
  }

  @Override
  protected OffsetStore createOffsetStore(MySqlBinlogCdcPipelineProperties properties,
                                          DataSource dataSource,
                                          EventuateSchema eventuateSchema,
                                          String clientName) {

    return new DatabaseOffsetKafkaStore(properties.getDbHistoryTopicName(),
            clientName,
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  @Override
  protected PublishingStrategy<PublishedEvent> createPublishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }
}
