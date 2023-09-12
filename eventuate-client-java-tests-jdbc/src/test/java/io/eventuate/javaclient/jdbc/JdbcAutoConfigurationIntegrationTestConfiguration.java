package io.eventuate.javaclient.jdbc;

import io.eventuate.example.banking.spring.services.JavaIntegrationTestDomainConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import(JavaIntegrationTestDomainConfiguration.class)
public class JdbcAutoConfigurationIntegrationTestConfiguration {
}
