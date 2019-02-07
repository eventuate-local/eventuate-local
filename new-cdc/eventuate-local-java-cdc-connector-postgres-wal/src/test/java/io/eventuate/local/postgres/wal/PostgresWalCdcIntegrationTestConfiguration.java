package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducer;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.local.test.util.SourceTableNameSupplier;
import io.eventuate.local.testutil.SqlScriptEditor;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.stream.Collectors;

@Configuration
@EnableAutoConfiguration
@Import(EventuateDriverConfiguration.class)
@EnableConfigurationProperties({EventuateKafkaProducerConfigurationProperties.class,
        EventuateKafkaConsumerConfigurationProperties.class})
public class PostgresWalCdcIntegrationTestConfiguration {

  @Bean
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName() == null ? "events" : eventuateConfigurationProperties.getSourceTableName());
  }

  @Bean
  public EventuateConfigurationProperties eventuateConfigurationProperties() {
    return new EventuateConfigurationProperties();
  }

  @Bean
  public PostgresWalClient postgresWalClient(CdcDataPublisher cdcDataPublisher,
                                             MeterRegistry meterRegistry,
                                             @Value("${spring.datasource.url}") String dbUrl,
                                             @Value("${spring.datasource.username}") String dbUserName,
                                             @Value("${spring.datasource.password}") String dbPassword,
                                             DataSource dataSource,
                                             EventuateConfigurationProperties eventuateConfigurationProperties,
                                             CuratorFramework curatorFramework) {

    return new PostgresWalClient(cdcDataPublisher,
            meterRegistry,
            dbUrl,
            dbUserName,
            dbPassword,
            eventuateConfigurationProperties.getPostgresWalIntervalInMilliseconds(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            eventuateConfigurationProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPostgresReplicationSlotName(),
            curatorFramework,
            eventuateConfigurationProperties.getLeadershipLockPath(),
            dataSource,
            eventuateConfigurationProperties.getBinlogClientId(),
            eventuateConfigurationProperties.getReplicationLagMeasuringIntervalInMilliseconds(),
            eventuateConfigurationProperties.getMonitoringRetryIntervalInMilliseconds(),
            eventuateConfigurationProperties.getMonitoringRetryAttempts(),
            eventuateConfigurationProperties.getAdditionalServiceReplicationSlotName(),
            eventuateConfigurationProperties.getWaitForOffsetSyncTimeoutInMilliseconds());
  }


  @Bean
  public CdcDataPublisher<PublishedEvent> dbLogBasedCdcKafkaPublisher(DataProducer dataProducer,
                                                                      PublishingStrategy<PublishedEvent> publishingStrategy,
                                                                      MeterRegistry meterRegistry) {

    return new CdcDataPublisher<>(dataProducer,
            publishingStrategy,
            meterRegistry);
  }

  @Bean
  public DataProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                       EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            eventuateKafkaProducerConfigurationProperties);
  }

  @Bean
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }

  @Bean
  public SqlScriptEditor sqlScriptEditor() {
    return sqlList -> {
      sqlList.set(0, sqlList.get(0).replace("CREATE SCHEMA", "CREATE SCHEMA IF NOT EXISTS"));

      return sqlList.stream().map(s -> s.replace("eventuate", "custom")).collect(Collectors.toList());
    };
  }

  @Bean
  public EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties() {
    return new EventuateLocalZookeperConfigurationProperties();
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
