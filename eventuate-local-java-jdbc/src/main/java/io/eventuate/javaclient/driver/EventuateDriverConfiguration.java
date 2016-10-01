package io.eventuate.javaclient.driver;

import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateLocalConfiguration.class)
public class EventuateDriverConfiguration {
}
