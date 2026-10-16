package io.eventuate.javaclient.spring;

import io.eventuate.javaclient.spring.crud.EventuateJavaClientDomainCrudConfiguration;
import io.eventuate.javaclient.spring.events.EventuateJavaClientDomainEventsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({EventuateJavaClientDomainCrudConfiguration.class, EventuateJavaClientDomainEventsConfiguration.class})
public class EventuateJavaClientDomainConfiguration {
}
