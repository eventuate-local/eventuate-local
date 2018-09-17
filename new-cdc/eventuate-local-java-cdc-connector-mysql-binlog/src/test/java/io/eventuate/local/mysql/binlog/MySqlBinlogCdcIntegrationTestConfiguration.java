package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.DbLogBasedCdcDataPublisher;
import io.eventuate.local.db.log.common.DuplicatePublishingDetector;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalJdbcAccess;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.function.Consumer;

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
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName(), "events");
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public MySqlBinaryLogClient mySqlBinaryLogClient(@Value("${spring.datasource.url}") String dataSourceURL,
                                                   EventuateConfigurationProperties eventuateConfigurationProperties) {

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    return new MySqlBinaryLogClient(
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            eventuateConfigurationProperties.getBinlogClientId(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
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
  @Conditional(MySqlBinlogCondition.class)
  @Primary
  public OffsetStore databaseOffsetKafkaStore(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                           EventuateConfigurationProperties eventuateConfigurationProperties,
                                                           MySqlBinaryLogClient mySqlBinaryLogClient,
                                                           EventuateKafkaProducer eventuateKafkaProducer,
                                                           EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
            mySqlBinaryLogClient.getName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public CdcProcessor<PublishedEvent> cdcProcessor(MySqlBinaryLogClient mySqlBinaryLogClient,
                                                   OffsetStore offsetStore,
                                                   DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore,
                                                   DataSource dataSource,
                                                   @Value("${spring.datasource.url}") String dataSourceUrl,
                                                   SourceTableNameSupplier sourceTableNameSupplier,
                                                   EventuateSchema eventuateSchema) {

    return new MySQLCdcProcessor<PublishedEvent>(mySqlBinaryLogClient,
            offsetStore,
            debeziumBinlogOffsetKafkaStore,
            new BinlogEntryToPublishedEventConverter(),
            dataSource,
            dataSourceUrl,
            sourceTableNameSupplier.getSourceTableName(),
            eventuateSchema) {
      @Override
      public void start(Consumer<PublishedEvent> publishedEventConsumer) {
        super.start(publishedEventConsumer);
        mySqlBinaryLogClient.start();
      }
    };
  }

  @Bean
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return new DebeziumBinlogOffsetKafkaStore(eventuateConfigurationProperties.getOldDbHistoryTopicName(),
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public CdcDataPublisher<PublishedEvent> cdcKafkaPublisher(DataProducerFactory dataProducerFactory,
                                                            EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                            EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                            OffsetStore offsetStore,
                                                            PublishingStrategy<PublishedEvent> publishingStrategy) {

    return new DbLogBasedCdcDataPublisher<>(dataProducerFactory,
            offsetStore,
            new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(), eventuateKafkaConsumerConfigurationProperties),
            publishingStrategy);
  }
}
