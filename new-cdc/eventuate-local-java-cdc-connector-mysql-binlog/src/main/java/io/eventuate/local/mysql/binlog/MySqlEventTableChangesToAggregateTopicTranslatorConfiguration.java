package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;


import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
@EnableConfigurationProperties({EventuateConfigurationProperties.class})
@Import({EventuateDriverConfiguration.class, EventTableChangesToAggregateTopicTranslatorConfiguration.class})
public class MySqlEventTableChangesToAggregateTopicTranslatorConfiguration {

  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  @Conditional(MysqlBinlogCondition.class)
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName(), "EVENTS");
  }

  @Bean
  @Conditional(MysqlBinlogCondition.class)
  public IWriteRowsEventDataParser eventDataParser(EventuateSchema eventuateSchema,
          EventuateConfigurationProperties eventuateConfigurationProperties,
          DataSource dataSource,
          SourceTableNameSupplier sourceTableNameSupplier) {
    return new WriteRowsEventDataParser(dataSource, sourceTableNameSupplier.getSourceTableName(), eventuateSchema);
  }

  @Bean
  @Conditional(MysqlBinlogCondition.class)
  public MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient(@Value("${spring.datasource.url}") String dataSourceURL,
          EventuateConfigurationProperties eventuateConfigurationProperties,
          SourceTableNameSupplier sourceTableNameSupplier,
          IWriteRowsEventDataParser<PublishedEvent> eventDataParser) throws IOException, TimeoutException {

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
  @Conditional(MysqlBinlogCondition.class)
  public CdcKafkaPublisher<PublishedEvent> mySQLCdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties, DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore, PublishingStrategy<PublishedEvent> publishingStrategy) {
    return new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore, eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }

  @Bean
  @Conditional(MysqlBinlogCondition.class)
  public CdcProcessor<PublishedEvent> mySQLCdcProcessor(MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient,
          DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore,
          DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore, debeziumBinlogOffsetKafkaStore);
  }

  @Bean
  @Conditional(MysqlBinlogCondition.class)
  public DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
          EventuateConfigurationProperties eventuateConfigurationProperties,
          MySqlBinaryLogClient mySqlBinaryLogClient,
          EventuateKafkaProducer eventuateKafkaProducer) {

    return new DatabaseBinlogOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(), mySqlBinaryLogClient.getName(), eventuateKafkaProducer, eventuateKafkaConfigurationProperties);
  }

  @Bean
  @Conditional(MysqlBinlogCondition.class)
  public DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
          EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {

    return new DebeziumBinlogOffsetKafkaStore(eventuateConfigurationProperties.getOldDbHistoryTopicName(), eventuateKafkaConfigurationProperties);
  }
}
