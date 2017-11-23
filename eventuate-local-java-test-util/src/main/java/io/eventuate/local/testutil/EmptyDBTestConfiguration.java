package io.eventuate.local.testutil;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"/emptydb.properties"})
@EnableAutoConfiguration
public class EmptyDBTestConfiguration {
}
