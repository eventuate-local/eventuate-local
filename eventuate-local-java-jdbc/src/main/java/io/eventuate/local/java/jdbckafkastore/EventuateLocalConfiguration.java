package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.AggregateEvents;
import io.eventuate.javaclient.commonimpl.SerializedEventDeserializer;
import io.eventuate.javaclient.commonimpl.adapters.AsyncToSyncAggregateEventsAdapter;
import io.eventuate.javaclient.commonimpl.adapters.AsyncToSyncTimeoutOptions;
import io.eventuate.javaclient.commonimpl.adapters.SyncToAsyncAggregateCrudAdapter;
import io.eventuate.javaclient.spring.common.EventuateCommonConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.EventuateKafkaPropertiesConfiguration;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.sql.dialect.SqlDialectConfiguration;
import io.eventuate.sql.dialect.SqlDialectSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Defines the Spring beans for the JDBC-based aggregate store
 */
@Configuration
@EnableTransactionManagement
@Import({EventuateCommonConfiguration.class, EventuateKafkaPropertiesConfiguration.class})
@EnableConfigurationProperties(EventuateKafkaConsumerConfigurationProperties.class)
public class EventuateLocalConfiguration {

  @Autowired(required=false)
  private SerializedEventDeserializer serializedEventDeserializer;

  @Autowired(required=false)
  private AsyncToSyncTimeoutOptions timeoutOptions;

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  public EventuateJdbcAccess eventuateJdbcAccess(EventuateSchema eventuateSchema, DataSource db) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
    return new EventuateLocalJdbcAccess(jdbcTemplate, eventuateSchema);
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
  public EventuateKafkaAggregateSubscriptions aggregateEvents(EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration,
                                                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    return new EventuateKafkaAggregateSubscriptions(eventuateLocalAggregateStoreConfiguration, eventuateKafkaConsumerConfigurationProperties);
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
