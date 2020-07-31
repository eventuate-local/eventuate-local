package io.eventuate.local.java.micronaut.jdbc.events;

import io.eventuate.javaclient.commonimpl.common.adapters.AsyncToSyncTimeoutOptions;
import io.eventuate.javaclient.commonimpl.events.AggregateEvents;
import io.eventuate.javaclient.commonimpl.events.adapters.AsyncToSyncAggregateEventsAdapter;
import io.eventuate.local.java.events.EventuateKafkaAggregateSubscriptions;
import io.eventuate.messaging.kafka.basic.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.messaging.kafka.basic.consumer.KafkaConsumerFactory;
import io.eventuate.messaging.kafka.common.EventuateKafkaConfigurationProperties;
import io.micronaut.context.annotation.Factory;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Factory
public class EventuateLocalEventsFactory {
  @Singleton
  public EventuateKafkaAggregateSubscriptions aggregateEvents(EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration,
                                                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                              KafkaConsumerFactory kafkaConsumerFactory) {
    return new EventuateKafkaAggregateSubscriptions(eventuateLocalAggregateStoreConfiguration,
            eventuateKafkaConsumerConfigurationProperties,
            kafkaConsumerFactory);
  }


  @Singleton
  public io.eventuate.javaclient.commonimpl.events.sync.AggregateEvents syncAggregateEvents(@Nullable AsyncToSyncTimeoutOptions timeoutOptions,
                                                                                            AggregateEvents aggregateEvents) {
    AsyncToSyncAggregateEventsAdapter adapter = new AsyncToSyncAggregateEventsAdapter(aggregateEvents);
    if (timeoutOptions != null)
      adapter.setTimeoutOptions(timeoutOptions);
    return adapter;
  }
}
