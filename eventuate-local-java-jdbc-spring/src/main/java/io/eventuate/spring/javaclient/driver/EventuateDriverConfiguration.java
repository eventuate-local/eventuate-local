package io.eventuate.spring.javaclient.driver;

import io.eventuate.spring.common.jdbckafkastore.EventuateLocalConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateLocalConfiguration.class)
public class EventuateDriverConfiguration {
}
