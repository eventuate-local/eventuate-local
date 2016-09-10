package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.javaclient.commonimpl.EventuateAggregateStoreImpl;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.AggregateEvents;
import io.eventuate.EventuateAggregateStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
public class EventuateJdbcEventStoreConfiguration {

  @Bean
  @ConditionalOnMissingBean(AggregateCrud.class)
  public EventuateJdbcAggregateStore eventuateJdbcEventStore(DataSource db) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
    return new EventuateJdbcAggregateStore(jdbcTemplate);
  }

  @Bean
  @ConditionalOnMissingBean(EventuateAggregateStore.class)
  public EventuateAggregateStore httpStompEventStore(AggregateCrud restClient, AggregateEvents stompClient) {
    return new EventuateAggregateStoreImpl(restClient, stompClient);
  }

  @Bean
  @ConditionalOnMissingBean(AggregateEvents.class)
  public EventuateKafkaAggregateSubscriptions eventuateKafkaAggregateSubscriptions(EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration) {
    return new EventuateKafkaAggregateSubscriptions(eventuateLocalAggregateStoreConfiguration);
  }
}
