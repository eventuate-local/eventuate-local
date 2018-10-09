package io.eventuate.local.unified.cdc.pipeline.common.configuration;

import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
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
  public SourceTableNameSupplier sourceTableNameSupplier() {
    return new SourceTableNameSupplier("events");
  }

  @Bean
  public OffsetStoreFactory offsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                               EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                               EventuateKafkaProducer eventuateKafkaProducer) {

    return (properties, dataSource, eventuateSchema, clientName) ->
            new DatabaseOffsetKafkaStore(properties.getDbHistoryTopicName(),
                    clientName,
                    eventuateKafkaProducer,
                    eventuateKafkaConfigurationProperties,
                    eventuateKafkaConsumerConfigurationProperties);
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
  public DataProducerFactory dataProducerFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                 EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {

    return () -> new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            eventuateKafkaProducerConfigurationProperties);
  }

  @Bean
  public EventuateConfigurationProperties eventuateConfigurationProperties() {
    return new EventuateConfigurationProperties();
  }

  @Bean
  public EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties() {
    return new EventuateLocalZookeperConfigurationProperties();
  }

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                       EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {

    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            eventuateKafkaProducerConfigurationProperties);
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    String connectionString = eventuateLocalZookeperConfigurationProperties.getConnectionString();
    return makeStartedCuratorClient(connectionString);
  }

  @Bean
  public PublishingFilter createPublishingFilter(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                 EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    return new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            eventuateKafkaConsumerConfigurationProperties);
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
