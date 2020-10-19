package io.eventuate.local.java.spring.common.jdbckafkastore;

import io.eventuate.javaclient.spring.EventuateJavaClientDomainConfiguration;
import io.eventuate.javaclient.spring.common.EventuateCommonConfiguration;
import io.eventuate.local.java.spring.events.EventuateLocalEventsConfiguration;
import io.eventuate.local.java.spring.jdbc.crud.EventuateLocalCrudConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EventuateLocalCrudConfiguration.class,
        EventuateLocalEventsConfiguration.class,
        EventuateCommonConfiguration.class,
        EventuateJavaClientDomainConfiguration.class})
public class EventuateLocalConfiguration {
}
