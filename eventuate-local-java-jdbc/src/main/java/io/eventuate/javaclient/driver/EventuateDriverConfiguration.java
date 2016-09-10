package io.eventuate.javaclient.driver;

import io.eventuate.javaclient.spring.httpstomp.EventuateHttpStompClientConfiguration;
import io.eventuate.local.java.jdbckafkastore.EventuateJdbcEventStoreConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateJdbcEventStoreConfiguration.class)
public class EventuateDriverConfiguration {
}
