package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.javaclient.commonimpl.EventuateAggregateStoreImpl;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.AggregateEvents;
import io.eventuate.EventuateAggregateStore;
import io.eventuate.javaclient.commonimpl.SerializedEventDeserializer;
import io.eventuate.javaclient.commonimpl.adapters.AsyncToSyncAggregateEventsAdapter;
import io.eventuate.javaclient.commonimpl.adapters.AsyncToSyncTimeoutOptions;
import io.eventuate.javaclient.commonimpl.adapters.SyncToAsyncAggregateCrudAdapter;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Defines the Spring beans for the JDBC-based aggregate store
 */
@Configuration
@EnableConfigurationProperties(EventuateKafkaConfigurationProperties.class)
@EnableTransactionManagement
public class EventuateLocalConfiguration {

  @Autowired(required=false)
  private SerializedEventDeserializer serializedEventDeserializer;

  @Autowired(required=false)
  private AsyncToSyncTimeoutOptions timeoutOptions;

  // CRUD

  @Bean
  public EventuateLocalAggregateCrud eventuateLocalAggregateCrud(DataSource db) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
    return new EventuateLocalAggregateCrud(jdbcTemplate);
  }

  @Bean
  public AggregateCrud asyncAggregateCrud(io.eventuate.javaclient.commonimpl.sync.AggregateCrud aggregateCrud) {
    return new SyncToAsyncAggregateCrudAdapter(aggregateCrud);
  }


  // Events

  @Bean
  public EventuateKafkaAggregateSubscriptions aggregateEvents(EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration) {
    return new EventuateKafkaAggregateSubscriptions(eventuateLocalAggregateStoreConfiguration);
  }


  @Bean
  public io.eventuate.javaclient.commonimpl.sync.AggregateEvents syncAggregateEvents(AggregateEvents aggregateEvents) {
    AsyncToSyncAggregateEventsAdapter adapter = new AsyncToSyncAggregateEventsAdapter(aggregateEvents);
    if (timeoutOptions != null)
      adapter.setTimeoutOptions(timeoutOptions);
    return adapter;
  }

  // Aggregate Store

  @Bean
  @ConditionalOnMissingBean(EventuateAggregateStore.class)
  public EventuateAggregateStore eventuateAggregateStore(AggregateCrud restClient, AggregateEvents stompClient) {
    EventuateAggregateStoreImpl eventuateAggregateStore = new EventuateAggregateStoreImpl(restClient, stompClient);
    if (serializedEventDeserializer != null)
      eventuateAggregateStore.setSerializedEventDeserializer(serializedEventDeserializer);
    return eventuateAggregateStore;
  }

  @Bean
  public io.eventuate.sync.EventuateAggregateStore syncEventuateAggregateStore(io.eventuate.javaclient.commonimpl.sync.AggregateCrud aggregateCrud,
                                                                               io.eventuate.javaclient.commonimpl.sync.AggregateEvents aggregateEvents) {
    return new io.eventuate.javaclient.commonimpl.sync.EventuateAggregateStoreImpl(aggregateCrud, aggregateEvents);
  }

}
