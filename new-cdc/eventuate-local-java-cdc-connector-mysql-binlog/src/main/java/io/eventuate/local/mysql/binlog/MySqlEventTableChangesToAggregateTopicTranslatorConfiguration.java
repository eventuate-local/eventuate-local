package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.CommonReplicationEventTableChangesToAggregateTopicTranslatorConfiguration;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.springframework.context.annotation.Conditional;
import javax.sql.DataSource;

@Configuration
@Import({CommonReplicationEventTableChangesToAggregateTopicTranslatorConfiguration.class, EventuateDriverConfiguration.class, EventTableChangesToAggregateTopicTranslatorConfiguration.class})
@Conditional(MySqlBinlogCondition.class)
public class MySqlEventTableChangesToAggregateTopicTranslatorConfiguration {

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName(), "EVENTS");
  }

  @Bean
  public IWriteRowsEventDataParser eventDataParser(EventuateSchema eventuateSchema,
                                                   DataSource dataSource,
                                                   SourceTableNameSupplier sourceTableNameSupplier) {

    return new WriteRowsEventDataParser(dataSource, sourceTableNameSupplier.getSourceTableName(), eventuateSchema);
  }

  @Bean
  public DbLogClient<PublishedEvent> dbLogClient(@Value("${spring.datasource.url}") String dataSourceURL,
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
  public DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {

    return new DebeziumBinlogOffsetKafkaStore(eventuateConfigurationProperties.getOldDbHistoryTopicName(),
            eventuateKafkaConfigurationProperties);
  }
}
