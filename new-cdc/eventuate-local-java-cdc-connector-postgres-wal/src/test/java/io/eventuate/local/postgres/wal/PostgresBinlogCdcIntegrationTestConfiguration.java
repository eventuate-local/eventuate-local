package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.ReplicationLogBasedCdcKafkaPublisher;
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
public class PostgresBinlogCdcIntegrationTestConfiguration {

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  @Profile("PostgresWal")
  public PostgresWalClient<PublishedEvent> postgresBinaryLogClien(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                  PostgresWalMessageParser<PublishedEvent> postgresWalMessageParser) {
    return new PostgresWalClient<>(postgresWalMessageParser,
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());
  }

  @Bean
  @Profile("PostgresWal")
  public PostgresWalMessageParser<PublishedEvent> postgresReplicationMessageParser() {
    return new PostgresWalJsonMessageParser();
  }

  @Bean
  @Profile("PostgresWal")
  public DatabaseOffsetKafkaStore databaseLastSequenceNumberKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
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
  public CdcProcessor<PublishedEvent> postgresCdcProcessor(PostgresWalClient<PublishedEvent> mySqlBinaryLogClient,
          DatabaseOffsetKafkaStore databaseOffsetKafkaStore) {
    return new PostgresWalCdcProcessor<>(mySqlBinaryLogClient, databaseOffsetKafkaStore);
  }

  @Bean
  @Profile("PostgresWal")
  public CdcKafkaPublisher<PublishedEvent> cdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
          DatabaseOffsetKafkaStore databaseOffsetKafkaStore,
          PublishingStrategy<PublishedEvent> publishingStrategy) {
    return new ReplicationLogBasedCdcKafkaPublisher<>(databaseOffsetKafkaStore, eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
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
