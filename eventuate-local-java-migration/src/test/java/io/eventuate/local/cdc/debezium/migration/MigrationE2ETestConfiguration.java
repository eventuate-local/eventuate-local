package io.eventuate.local.cdc.debezium.migration;

import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import(EventuateLocalConfiguration.class)
public class MigrationE2ETestConfiguration {
}
