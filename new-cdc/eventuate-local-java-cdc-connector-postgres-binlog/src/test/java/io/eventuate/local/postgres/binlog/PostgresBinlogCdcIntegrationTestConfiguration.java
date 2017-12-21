package io.eventuate.local.postgres.binlog;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@Import(EventuateDriverConfiguration.class)
public class PostgresBinlogCdcIntegrationTestConfiguration {

  @Bean
  public EventuateConfigurationProperties eventuateConfigurationProperties() {
    return new EventuateConfigurationProperties();
  }

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  @Profile("PostgresWal")
  public PostgresBinaryLogClient<PublishedEvent> postgresBinaryLogClien(DataSource dataSource,
          PostgresReplicationMessageParser<PublishedEvent> postgresReplicationMessageParser) {
    return new PostgresBinaryLogClient<>(dataSource, postgresReplicationMessageParser);
  }

  @Bean
  @Profile("PostgresWal")
  public PostgresReplicationMessageParser<PublishedEvent> postgresReplicationMessageParser() {
    return new PostgresJsonReplicationMessageParser();
  }

  @Bean
  @Profile("PostgresWal")
  public DatabaseLastSequenceNumberKafkaStore databaseLastSequenceNumberKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
          EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
          EventuateKafkaProducer eventuateKafkaProducer) {

    return new DatabaseLastSequenceNumberKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties
    );
  }

  @Bean
  @Profile("PostgresWal")
  public CdcProcessor<PublishedEvent> postgresCdcProcessor(PostgresBinaryLogClient<PublishedEvent> mySqlBinaryLogClient,
          DatabaseLastSequenceNumberKafkaStore databaseLastSequenceNumberKafkaStore) {
    return new PostgresCdcProcessor<>(mySqlBinaryLogClient, databaseLastSequenceNumberKafkaStore);
  }

  @Bean
  @Profile("PostgresWal")
  public CdcKafkaPublisher<PublishedEvent> cdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
          DatabaseLastSequenceNumberKafkaStore databaseLastSequenceNumberKafkaStore,
          PublishingStrategy<PublishedEvent> publishingStrategy) {
    return new PostgresCdcKafkaPublisher<>(databaseLastSequenceNumberKafkaStore, eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
  }

  @Bean
  public PublishingStrategy<PublishedEvent> publishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }
}
