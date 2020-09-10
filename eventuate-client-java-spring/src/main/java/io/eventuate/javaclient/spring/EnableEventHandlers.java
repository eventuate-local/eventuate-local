package io.eventuate.javaclient.spring;

import io.eventuate.javaclient.spring.events.EventuateJavaClientDomainEventsConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures event handling Spring beans
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EventuateJavaClientDomainConfiguration.class)
public @interface EnableEventHandlers {
}
