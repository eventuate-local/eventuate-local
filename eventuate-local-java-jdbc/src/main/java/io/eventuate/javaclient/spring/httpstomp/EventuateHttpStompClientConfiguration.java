package io.eventuate.javaclient.spring.httpstomp;

import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Defines the Spring beans to connect to Eventuate Local
 *
 * @deprecated  use {@link io.eventuate.javaclient.driver.EventuateDriverConfiguration} instead
 * @see io.eventuate.javaclient.driver.EventuateDriverConfiguration
 */

@Configuration
@Import(EventuateLocalConfiguration.class)
public class EventuateHttpStompClientConfiguration {
}
