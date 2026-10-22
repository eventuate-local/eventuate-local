package io.eventuate.local.java.spring.autoconfigure;

import io.eventuate.local.java.spring.javaclient.driver.EventuateDriverConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(EventuateDriverConfiguration.class)
public class EventuateDriverAutoConfiguration {
}
