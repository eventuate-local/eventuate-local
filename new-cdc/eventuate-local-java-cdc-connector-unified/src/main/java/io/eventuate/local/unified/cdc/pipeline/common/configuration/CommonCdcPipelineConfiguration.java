package io.eventuate.local.unified.cdc.pipeline.common.configuration;

import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplateFactory;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.KafkaCdcDataPublisherTransactionTemplate;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.local.unified.cdc.pipeline.common.health.BinlogEntryReaderHealthCheck;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.DefaultSourceTableNameResolver;
import io.eventuate.local.unified.cdc.pipeline.common.health.CdcDataPublisherHealthCheck;
import io.eventuate.local.unified.cdc.pipeline.common.health.KafkaHealthCheck;
import io.eventuate.local.unified.cdc.pipeline.common.health.ZookeeperHealthCheck;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.DebeziumOffsetStoreFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties({EventuateKafkaProducerConfigurationProperties.class,
        EventuateKafkaConsumerConfigurationProperties.class})
public class CommonCdcPipelineConfiguration {

  @Bean
  public DataProducerFactory eventuateKafkaProducerFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                     EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {

  return () -> new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
          eventuateKafkaProducerConfigurationProperties);
  }

  @Bean
  public CdcDataPublisherTransactionTemplateFactory kafkaCdcDataPublisherTransactionTemplateFactory() {
    return (dataProducer) -> {
      if (!(dataProducer instanceof EventuateKafkaProducer)) {
        throw new IllegalArgumentException(String.format("Expected %s", EventuateKafkaProducer.class));
      }

      return new KafkaCdcDataPublisherTransactionTemplate((EventuateKafkaProducer)dataProducer);
    };
  }

  @Bean
  public OffsetStoreFactory offsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                               EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (properties, dataSource, eventuateSchema, clientName, dataProducer) -> {
      if (!(dataProducer instanceof EventuateKafkaProducer)) {
        throw new IllegalArgumentException(String.format("Expected %s", EventuateKafkaProducer.class));
      }

      return new DatabaseOffsetKafkaStore(properties.getDbHistoryTopicName(),
              clientName,
              (EventuateKafkaProducer) dataProducer,
              eventuateKafkaConfigurationProperties,
              eventuateKafkaConsumerConfigurationProperties);
    };
  }


  @Bean
  public BinlogEntryReaderHealthCheck binlogEntryReaderHealthCheck(BinlogEntryReaderProvider binlogEntryReaderProvider) {
    return new BinlogEntryReaderHealthCheck(binlogEntryReaderProvider);
  }

  @Bean
  public CdcDataPublisherHealthCheck cdcDataPublisherHealthCheck(DataProducerFactory dataProducerFactory,
                                                                 CdcDataPublisherFactory cdcDataPublisherFactory) {
    return new CdcDataPublisherHealthCheck(cdcDataPublisherFactory.create(dataProducerFactory.create()));
  }

  @Bean
  public ZookeeperHealthCheck zookeeperHealthCheck() {
    return new ZookeeperHealthCheck();
  }

  @Bean
  public KafkaHealthCheck kafkaHealthCheck() {
    return new KafkaHealthCheck();
  }

  @Bean
  public DefaultSourceTableNameResolver defaultSourceTableNameResolver() {
    return pipelineType -> {
      if ("eventuate-local".equals(pipelineType) || "default".equals(pipelineType)) return "events";

      throw new RuntimeException(String.format("Unknown pipeline type '%s'", pipelineType));
    };
  }

  @Bean
  public DebeziumOffsetStoreFactory mySqlBinLogOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                  EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (oldDbHistoryTopicName) ->
            new DebeziumBinlogOffsetKafkaStore(oldDbHistoryTopicName,
                    eventuateKafkaConfigurationProperties,
                    eventuateKafkaConsumerConfigurationProperties);
  }

  @Bean
  public BinlogEntryReaderProvider dbClientProvider() {
    return new BinlogEntryReaderProvider();
  }

  @Bean
  public EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties() {
    return new EventuateKafkaConfigurationProperties();
  }

  @Bean
  public EventuateConfigurationProperties eventuateConfigurationProperties() {
    return new EventuateConfigurationProperties();
  }

  @Bean
  public EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties() {
    return new EventuateLocalZookeperConfigurationProperties();
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    String connectionString = eventuateLocalZookeperConfigurationProperties.getConnectionString();
    return makeStartedCuratorClient(connectionString);
  }

  static CuratorFramework makeStartedCuratorClient(String connectionString) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(connectionString)
            .build();
    client.start();
    return client;
  }
}
