package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.AggregateEvents;
import io.eventuate.javaclient.commonimpl.SerializedEventDeserializer;
import io.eventuate.javaclient.commonimpl.adapters.AsyncToSyncAggregateEventsAdapter;
import io.eventuate.javaclient.commonimpl.adapters.AsyncToSyncTimeoutOptions;
import io.eventuate.javaclient.commonimpl.adapters.SyncToAsyncAggregateCrudAdapter;
import io.eventuate.javaclient.spring.common.EventuateCommonConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Defines the Spring beans for the JDBC-based aggregate store
 */
@Configuration
@EnableConfigurationProperties(EventuateKafkaConfigurationProperties.class)
@EnableTransactionManagement
@Import(EventuateCommonConfiguration.class)
public class EventuateLocalConfiguration {

  @Value("${eventuateLocal.cdc.eventuate.database:#{null}}")
  private String eventuateDatabase;

  @Autowired(required=false)
  private SerializedEventDeserializer serializedEventDeserializer;

  @Autowired(required=false)
  private AsyncToSyncTimeoutOptions timeoutOptions;

  // CRUD

  @Bean
  public EventuateJdbcAccess eventuateJdbcAccess(DataSource db) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
    return new EventuateLocalJdbcAccess(jdbcTemplate, Optional.ofNullable(eventuateDatabase));
  }

  @Bean
  public EventuateLocalAggregateCrud eventuateLocalAggregateCrud(EventuateJdbcAccess eventuateJdbcAccess) {
    return new EventuateLocalAggregateCrud(eventuateJdbcAccess);
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
  // Why @ConditionalOnMissingBean(EventuateAggregateStore.class)??


}
