package io.eventuate.local.unified.cdc.pipeline.common.configuration;

import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonEventuateLocalConfiguration {

  @Bean
  public OffsetStoreFactory offsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                               EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                               EventuateKafkaProducer eventuateKafkaProducer) {

    return (properties, dataSource, eventuateSchema, clientName) ->
            new DatabaseOffsetKafkaStore(properties.getDbHistoryTopicName(),
                    clientName,
                    eventuateKafkaProducer,
                    eventuateKafkaConfigurationProperties,
                    eventuateKafkaConsumerConfigurationProperties);
  }
}
