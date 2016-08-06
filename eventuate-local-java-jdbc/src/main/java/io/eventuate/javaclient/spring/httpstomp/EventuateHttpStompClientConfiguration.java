package io.eventuate.javaclient.spring.httpstomp;

import io.eventuate.local.java.jdbckafkastore.EventuateJdbcEventStoreConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateJdbcEventStoreConfiguration.class)
public class EventuateHttpStompClientConfiguration {
}
