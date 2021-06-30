package io.eventuate.javaclient.spring.jdbc;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Defines the Spring beans for the embedded, JDBC-based event store
 * @deprecated  use {@link EmbeddedTestAggregateStoreConfiguration} instead
 */
@Configuration
@Import(EmbeddedTestAggregateStoreConfiguration.class)
public class EventuateJdbcEventStoreConfiguration {

}
