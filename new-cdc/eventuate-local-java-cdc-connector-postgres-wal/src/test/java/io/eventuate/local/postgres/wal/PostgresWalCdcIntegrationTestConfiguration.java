package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.DbLogBasedCdcKafkaPublisher;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAutoConfiguration
@Import(EventuateDriverConfiguration.class)
@EnableConfigurationProperties({EventuateConfigurationProperties.class})
public class PostgresWalCdcIntegrationTestConfiguration {

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  @Profile("PostgresWal")
  public PostgresWalClient<PublishedEvent> postgresWalClient(@Value("${spring.datasource.url}") String dbUrl,
                                                             @Value("${spring.datasource.username}") String dbUserName,
                                                             @Value("${spring.datasource.password}") String dbPassword,
                                                             EventuateConfigurationProperties eventuateConfigurationProperties,
                                                             PostgresWalMessageParser<PublishedEvent> postgresWalMessageParser) {

    return new PostgresWalClient<>(postgresWalMessageParser,
            dbUrl,
            dbUserName,
            dbPassword,
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            eventuateConfigurationProperties.getPostresWalIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPostgresReplicationSlotName());
  }

  @Bean
  @Profile("PostgresWal")
  public PostgresWalMessageParser<PublishedEvent> postgresWalMessageParser() {
    return new PostgresWalJsonMessageParser();
  }

  @Bean
  @Profile("PostgresWal")
  public DatabaseOffsetKafkaStore databaseOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                           EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                           EventuateKafkaProducer eventuateKafkaProducer) {

    return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties
    );
  }

  @Bean
  @Profile("PostgresWal")
  public CdcProcessor<PublishedEvent> cdcProcessor(PostgresWalClient<PublishedEvent> postgresWalClient,
                                                   DatabaseOffsetKafkaStore databaseOffsetKafkaStore) {

    return new PostgresWalCdcProcessor<>(postgresWalClient, databaseOffsetKafkaStore);
  }

  @Bean
  @Profile("PostgresWal")
  public DbLogBasedCdcKafkaPublisher<PublishedEvent> dbLogBasedCdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                             DatabaseOffsetKafkaStore databaseOffsetKafkaStore,
                                                             PublishingStrategy<PublishedEvent> publishingStrategy) {

    return new DbLogBasedCdcKafkaPublisher<>(databaseOffsetKafkaStore, eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
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
