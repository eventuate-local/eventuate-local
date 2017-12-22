package io.eventuate.local.db.log.common;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.common.*;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import({EventuateDriverConfiguration.class, EventTableChangesToAggregateTopicTranslatorConfiguration.class})
public class CommonReplicationEventTableChangesToAggregateTopicTranslatorConfiguration {

    @Bean
    @Profile("!EventuatePolling")
    public DatabaseOffsetKafkaStore databaseLastSequenceNumberKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                         EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                         EventuateKafkaProducer eventuateKafkaProducer) {

      return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
              eventuateConfigurationProperties.getMySqlBinLogClientName(),
              eventuateKafkaProducer,
              eventuateKafkaConfigurationProperties);
    }

    @Bean
    @Profile("!EventuatePolling")
    public CdcKafkaPublisher<PublishedEvent> cdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                               DatabaseOffsetKafkaStore databaseOffsetKafkaStore,
                                                               PublishingStrategy<PublishedEvent> publishingStrategy) {

        return new ReplicationLogBasedCdcKafkaPublisher<>(databaseOffsetKafkaStore,
                eventuateKafkaConfigurationProperties.getBootstrapServers(),
                publishingStrategy);
    }

    @Bean
    @Profile("!EventuatePolling")
    public CdcProcessor<PublishedEvent> postgresCdcProcessor(ReplicationLogClient<PublishedEvent> replicationLogClient,
                                                             DatabaseOffsetKafkaStore databaseOffsetKafkaStore) {

      return new ReplicationLogBasedCdcProcessor<>(replicationLogClient, databaseOffsetKafkaStore);
    }
}
