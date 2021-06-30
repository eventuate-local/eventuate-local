package io.eventuate.local.java.spring.javaclient.driver;

import io.eventuate.local.java.spring.common.jdbckafkastore.EventuateLocalConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateLocalConfiguration.class)
public class EventuateDriverConfiguration {
}
