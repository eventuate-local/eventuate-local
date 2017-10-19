package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.common.*;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
@EnableConfigurationProperties({EventuateConfigurationProperties.class})
@Import({EventuateDriverConfiguration.class, EventTableChangesToAggregateTopicTranslatorConfiguration.class})
public class MySqlEventTableChangesToAggregateTopicTranslatorConfiguration {

  @Bean
  @Profile("!EventuatePolling")
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName(), "EVENTS");
  }

  @Bean
  @Profile("!EventuatePolling")
  public IWriteRowsEventDataParser eventDataParser(DataSource dataSource, SourceTableNameSupplier sourceTableNameSupplier) {
    return new WriteRowsEventDataParser(dataSource, sourceTableNameSupplier.getSourceTableName());
  }

  @Bean
  @Profile("!EventuatePolling")
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
            eventuateConfigurationProperties.getMySqlBinLogClientName());
  }

  @Bean
  @Profile("!EventuatePolling")
  public CdcKafkaPublisher<PublishedEvent> mySQLCdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties, DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore, PublishingStrategy<PublishedEvent> publishingStrategy) {
    return new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore, eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }

  @Bean
  @Profile("!EventuatePolling")
  public CdcProcessor<PublishedEvent> mySQLCdcProcessor(MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient, DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore) {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore);
  }

  @Bean
  @Profile("!EventuatePolling")
  public DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
          EventuateConfigurationProperties eventuateConfigurationProperties,
          MySqlBinaryLogClient mySqlBinaryLogClient,
          EventuateKafkaProducer eventuateKafkaProducer) {

    return new DatabaseBinlogOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(), mySqlBinaryLogClient.getName(), eventuateKafkaProducer, eventuateKafkaConfigurationProperties);
  }
}
