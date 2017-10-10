package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalJdbcAccess;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties({MySqlBinaryLogClientConfigurationProperties.class, EventuateLocalZookeperConfigurationProperties.class})
@Import(EventuateDriverConfiguration.class)
public class MySqlBinlogCdcIntegrationTestConfiguration {

  @Bean
  @Profile("!EventuatePolling")
  public SourceTableNameSupplier sourceTableNameSupplier(MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties) {
    return new SourceTableNameSupplier(mySqlBinaryLogClientConfigurationProperties.getSourceTableName(), "EVENTS");
  }

  @Bean
  @Profile("!EventuatePolling")
  public MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient(@Value("${spring.datasource.url}") String dataSourceURL,
                                                                   MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties,
                                                                   SourceTableNameSupplier sourceTableNameSupplier,
                                                                   IWriteRowsEventDataParser<PublishedEvent> eventDataParser) throws IOException, TimeoutException {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    return new MySqlBinaryLogClient<>(eventDataParser,
            mySqlBinaryLogClientConfigurationProperties.getDbUserName(),
            mySqlBinaryLogClientConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            mySqlBinaryLogClientConfigurationProperties.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName());
  }

  @Bean
  @Profile("!EventuatePolling")
  public EventuateJdbcAccess eventuateJdbcAccess(DataSource db) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
    return new EventuateLocalJdbcAccess(jdbcTemplate);
  }

  @Bean
  @Profile("!EventuatePolling")
  public IWriteRowsEventDataParser<PublishedEvent> eventDataParser(DataSource dataSource,
                                                                   SourceTableNameSupplier sourceTableNameSupplier) {
    return new WriteRowsEventDataParser(dataSource, sourceTableNameSupplier.getSourceTableName());
  }

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
  }

  @Bean
  @Profile("!EventuatePolling")
  public DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                 MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties,
                                                                 MySqlBinaryLogClient mySqlBinaryLogClient,
                                                                 EventuateKafkaProducer eventuateKafkaProducer) {
    return new DatabaseBinlogOffsetKafkaStore(mySqlBinaryLogClientConfigurationProperties.getDbHistoryTopicName(), mySqlBinaryLogClient.getName(), eventuateKafkaProducer, eventuateKafkaConfigurationProperties);
  }

  @Bean
  @Profile("!EventuatePolling")
  public MySQLCdcProcessor<PublishedEvent> mySQLCdcProcessor(MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient, DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore) {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore);
  }

  @Bean
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }


  @Bean
  @Profile("EventuatePolling")
  public PollingCdcKafkaPublisher<PublishedEvent> pollingCdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
    PublishingStrategy<PublishedEvent> publishingStrategy) {
    return new PollingCdcKafkaPublisher<>(eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingCdcProcessor<PublishedEventBean, PublishedEvent, String> pollingCdcProcessor(MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties,
    PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao) {
    return new PollingCdcProcessor<>(pollingDao, mySqlBinaryLogClientConfigurationProperties.getPollingIntervalInMilliseconds());
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDataProvider<PublishedEventBean, PublishedEvent, String> pollingDataProvider() {
    return new EventPollingDataProvider();
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao(MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties,
    PollingDataProvider<PublishedEventBean, PublishedEvent, String> pollingDataProvider,
    DataSource dataSource) {

    return new PollingDao<>(pollingDataProvider,
      dataSource,
      mySqlBinaryLogClientConfigurationProperties.getMaxEventsPerPolling(),
      mySqlBinaryLogClientConfigurationProperties.getMaxAttemptsForPolling(),
      mySqlBinaryLogClientConfigurationProperties.getPollingRetryIntervalInMilliseconds());
  }
}
