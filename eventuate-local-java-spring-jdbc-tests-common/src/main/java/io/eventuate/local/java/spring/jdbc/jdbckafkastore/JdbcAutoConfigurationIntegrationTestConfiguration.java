package io.eventuate.local.java.spring.jdbc.jdbckafkastore;

import io.eventuate.example.banking.services.spring.JavaIntegrationTestDomainConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import(JavaIntegrationTestDomainConfiguration.class)
public class JdbcAutoConfigurationIntegrationTestConfiguration {
}
