package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.DbLogBasedCdcKafkaPublisher;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalJdbcAccess;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties({EventuateConfigurationProperties.class, EventuateLocalZookeperConfigurationProperties.class})
@Import(EventuateDriverConfiguration.class)
public class MySqlBinlogCdcIntegrationTestConfiguration {

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName(), "EVENTS");
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient(@Value("${spring.datasource.url}") String dataSourceURL,
                                                                   EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                   SourceTableNameSupplier sourceTableNameSupplier,
                                                                   IWriteRowsEventDataParser<PublishedEvent> eventDataParser) {

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    return new MySqlBinaryLogClient<>(eventDataParser,
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            eventuateConfigurationProperties.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName(),
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
  @Conditional(MySqlBinlogCondition.class)
  public IWriteRowsEventDataParser<PublishedEvent> eventDataParser(EventuateSchema eventuateSchema,
                                                                   DataSource dataSource,
                                                                   SourceTableNameSupplier sourceTableNameSupplier) {

    return new WriteRowsEventDataParser(dataSource, sourceTableNameSupplier.getSourceTableName(), eventuateSchema);
  }

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public DatabaseOffsetKafkaStore databaseOffsetKafkaStore(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                           EventuateConfigurationProperties eventuateConfigurationProperties,
                                                           MySqlBinaryLogClient mySqlBinaryLogClient,
                                                           EventuateKafkaProducer eventuateKafkaProducer) {

    return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
            mySqlBinaryLogClient.getName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties);
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public CdcProcessor<PublishedEvent> cdcProcessor(MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient,
                                                   DatabaseOffsetKafkaStore binlogOffsetKafkaStore,
                                                   DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {

    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore, debeziumBinlogOffsetKafkaStore);
  }

  @Bean
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {

    return new DebeziumBinlogOffsetKafkaStore(eventuateConfigurationProperties.getOldDbHistoryTopicName(), eventuateKafkaConfigurationProperties);
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public CdcKafkaPublisher<PublishedEvent> cdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                             DatabaseOffsetKafkaStore binlogOffsetKafkaStore,
                                                             PublishingStrategy<PublishedEvent> publishingStrategy) {

    return new DbLogBasedCdcKafkaPublisher<>(binlogOffsetKafkaStore, eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }
}
