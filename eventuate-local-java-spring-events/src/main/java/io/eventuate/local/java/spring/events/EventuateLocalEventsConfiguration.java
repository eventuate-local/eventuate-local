package io.eventuate.local.java.spring.events;

import io.eventuate.javaclient.commonimpl.common.adapters.AsyncToSyncTimeoutOptions;
import io.eventuate.javaclient.commonimpl.events.AggregateEvents;
import io.eventuate.javaclient.commonimpl.events.adapters.AsyncToSyncAggregateEventsAdapter;
import io.eventuate.javaclient.spring.common.events.EventuateCommonEventsConfiguration;
import io.eventuate.local.java.events.EventuateKafkaAggregateSubscriptions;
import io.eventuate.messaging.kafka.basic.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.messaging.kafka.basic.consumer.KafkaConsumerFactory;
import io.eventuate.messaging.kafka.spring.basic.consumer.EventuateKafkaConsumerSpringConfigurationPropertiesConfiguration;
import io.eventuate.messaging.kafka.common.EventuateKafkaConfigurationProperties;
import io.eventuate.messaging.kafka.spring.common.EventuateKafkaPropertiesConfiguration;
import io.eventuate.messaging.kafka.spring.consumer.KafkaConsumerFactoryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EventuateCommonEventsConfiguration.class,
        EventuateKafkaPropertiesConfiguration.class,
        EventuateKafkaConsumerSpringConfigurationPropertiesConfiguration.class,
        KafkaConsumerFactoryConfiguration.class})
public class EventuateLocalEventsConfiguration {
  @Bean
  public EventuateKafkaAggregateSubscriptions aggregateEvents(EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration,
                                                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                              KafkaConsumerFactory kafkaConsumerFactory) {
    return new EventuateKafkaAggregateSubscriptions(eventuateLocalAggregateStoreConfiguration, eventuateKafkaConsumerConfigurationProperties, kafkaConsumerFactory);
  }


  @Bean
  public io.eventuate.javaclient.commonimpl.events.sync.AggregateEvents syncAggregateEvents(@Autowired(required = false) AsyncToSyncTimeoutOptions timeoutOptions,
                                                                                            AggregateEvents aggregateEvents) {
    AsyncToSyncAggregateEventsAdapter adapter = new AsyncToSyncAggregateEventsAdapter(aggregateEvents);
    if (timeoutOptions != null)
      adapter.setTimeoutOptions(timeoutOptions);
    return adapter;
  }
}
