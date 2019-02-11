package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.common.OffsetStoreCreator;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class KafkaOffsetStoreConfiguration {

  @Bean
  @Primary
  public OffsetStoreCreator offsetStoreCreator(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                               EventuateConfigurationProperties eventuateConfigurationProperties,
                                               EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return dataProducer -> {
      if (!(dataProducer instanceof EventuateKafkaProducer)) {
        throw new IllegalArgumentException(String.format("Expected %s", EventuateKafkaProducer.class));
      }

      return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getOffsetStorageTopicName(),
              eventuateConfigurationProperties.getMySqlBinlogClientName(),
              (EventuateKafkaProducer) dataProducer,
              eventuateKafkaConfigurationProperties,
              eventuateKafkaConsumerConfigurationProperties);
    };
  }
}
