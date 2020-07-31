package io.eventuate.javaclient.spring;

import io.eventuate.javaclient.spring.crud.EventuateJavaClientDomainCrudConfiguration;
import io.eventuate.javaclient.spring.events.EventuateJavaClientDomainEventsConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EventuateJavaClientDomainCrudConfiguration.class, EventuateJavaClientDomainEventsConfiguration.class})
public class EventuateJavaClientDomainConfiguration {
}
