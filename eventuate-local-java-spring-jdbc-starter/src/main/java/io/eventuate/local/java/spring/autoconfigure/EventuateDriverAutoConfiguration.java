package io.eventuate.local.java.spring.autoconfigure;

import io.eventuate.local.java.spring.javaclient.driver.EventuateDriverConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateDriverConfiguration.class)
public class EventuateDriverAutoConfiguration {
}
