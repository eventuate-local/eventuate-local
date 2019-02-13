package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplateFactory;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.KafkaCdcDataPublisherTransactionTemplate;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.local.test.util.SourceTableNameSupplier;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@EnableAutoConfiguration
@Import(EventuateDriverConfiguration.class)
@EnableConfigurationProperties({EventuateKafkaProducerConfigurationProperties.class,
        EventuateKafkaConsumerConfigurationProperties.class})
public class MySqlBinlogCdcIntegrationTestConfiguration {

  @Bean
  public EventuateConfigurationProperties eventuateConfigurationProperties() {
    return new EventuateConfigurationProperties();
  }

  @Bean
  public EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties() {
    return new EventuateLocalZookeperConfigurationProperties();
  }

  @Bean
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName() == null ? "events" : eventuateConfigurationProperties.getSourceTableName());
  }

  @Bean
  public MySqlBinaryLogClient mySqlBinaryLogClient(DataProducerFactory dataProducerFactory,
                                                   CdcDataPublisherFactory cdcDataPublisherFactory,
                                                   CdcDataPublisherTransactionTemplateFactory cdcDataPublisherTransactionTemplateFactory,
                                                   @Autowired MeterRegistry meterRegistry,
                                                   @Value("${spring.datasource.url}") String dataSourceURL,
                                                   DataSource dataSource,
                                                   EventuateConfigurationProperties eventuateConfigurationProperties,
                                                   CuratorFramework curatorFramework,
                                                   OffsetStoreCreator offsetStoreCreator) {

    return new MySqlBinaryLogClient(dataProducerFactory,
            cdcDataPublisherFactory,
            cdcDataPublisherTransactionTemplateFactory,
            meterRegistry,
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            dataSourceURL,
            dataSource,
            eventuateConfigurationProperties.getBinlogClientId(),
            eventuateConfigurationProperties.getMySqlBinlogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            curatorFramework,
            eventuateConfigurationProperties.getLeadershipLockPath(),
            offsetStoreCreator,
            Optional.empty(),
            eventuateConfigurationProperties.getReplicationLagMeasuringIntervalInMilliseconds(),
            eventuateConfigurationProperties.getMonitoringRetryIntervalInMilliseconds(),
            eventuateConfigurationProperties.getMonitoringRetryAttempts(),
            eventuateConfigurationProperties.isUseGTIDsWhenPossible());
  }

  @Bean
  public CdcDataPublisherFactory cdcDataPublisherFactory(MeterRegistry meterRegistry) {
    return dataProducer -> new CdcDataPublisher<>(dataProducer, meterRegistry);
  }

  @Bean
  public DataProducerFactory dataProducerFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                 EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {
    return (transactionalId) ->
              new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
                      eventuateKafkaProducerConfigurationProperties,
                      transactionalId);
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
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }

  @Bean
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(eventuateLocalZookeperConfigurationProperties.getConnectionString())
            .build();
    client.start();
    return client;
  }
}
