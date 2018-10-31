package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalJdbcAccess;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
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
import org.springframework.jdbc.core.JdbcTemplate;

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
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName() == null ? "events" : eventuateConfigurationProperties.getSourceTableName());
  }

  @Bean
  public MySqlBinaryLogClient mySqlBinaryLogClient(@Autowired(required = false) MeterRegistry meterRegistry,
                                                   @Value("${spring.datasource.url}") String dataSourceURL,
                                                   DataSource dataSource,
                                                   EventuateConfigurationProperties eventuateConfigurationProperties,
                                                   CuratorFramework curatorFramework,
                                                   OffsetStore offsetStore) {

    return new MySqlBinaryLogClient(
            meterRegistry,
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            dataSourceURL,
            dataSource,
            eventuateConfigurationProperties.getBinlogClientId(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            curatorFramework,
            eventuateConfigurationProperties.getLeadershipLockPath(),
            offsetStore,
            Optional.empty(),
            eventuateConfigurationProperties.getReplicationLagMeasuringIntervalInMilliseconds());
  }

  @Bean
  public EventuateJdbcAccess eventuateJdbcAccess(EventuateSchema eventuateSchema, DataSource db) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
    return new EventuateLocalJdbcAccess(jdbcTemplate, eventuateSchema);
  }

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                       EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            eventuateKafkaProducerConfigurationProperties);
  }

  @Bean
  public DataProducerFactory dataProducerFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                 EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {
    return () -> new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(), eventuateKafkaProducerConfigurationProperties);
  }

  @Bean
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }

  @Bean
  public CdcDataPublisher<PublishedEvent> cdcKafkaPublisher(DataProducerFactory dataProducerFactory,
                                                            EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                            EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                            PublishingStrategy<PublishedEvent> publishingStrategy) {

    return new CdcDataPublisher<>(dataProducerFactory,
            new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(), eventuateKafkaConsumerConfigurationProperties),
            publishingStrategy);
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
