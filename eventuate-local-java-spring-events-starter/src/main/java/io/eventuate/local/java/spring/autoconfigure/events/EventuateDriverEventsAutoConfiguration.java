package io.eventuate.local.java.spring.autoconfigure.events;

import io.eventuate.javaclient.spring.common.events.EventuateCommonEventsConfiguration;
import io.eventuate.javaclient.spring.events.EnableEventHandlers;
import io.eventuate.local.java.spring.events.EventuateLocalEventsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({EventuateLocalEventsConfiguration.class, EventuateCommonEventsConfiguration.class})
@EnableEventHandlers
public class EventuateDriverEventsAutoConfiguration {
}
