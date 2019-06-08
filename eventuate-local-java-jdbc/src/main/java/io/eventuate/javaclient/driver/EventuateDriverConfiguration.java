package io.eventuate.javaclient.driver;

import io.eventuate.common.jdbckafkastore.EventuateLocalConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateLocalConfiguration.class)
public class EventuateDriverConfiguration {
}
