package io.eventuate.local.java.spring.autoconfigure.crud;

import io.eventuate.javaclient.spring.common.crud.EventuateCommonCrudConfiguration;
import io.eventuate.local.java.spring.jdbc.crud.EventuateLocalCrudConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EventuateLocalCrudConfiguration.class, EventuateCommonCrudConfiguration.class})
public class EventuateDriverCrudAutoConfiguration {
}
