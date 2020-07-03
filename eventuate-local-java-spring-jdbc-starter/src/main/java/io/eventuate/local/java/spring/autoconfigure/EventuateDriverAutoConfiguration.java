package io.eventuate.local.java.spring.autoconfigure;

import io.eventuate.javaclient.spring.EnableEventHandlers;
import io.eventuate.local.java.spring.javaclient.driver.EventuateDriverConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(EventuateDriverConfiguration.class)
@Import(EventuateDriverConfiguration.class)
@EnableEventHandlers
public class EventuateDriverAutoConfiguration {
}
