package io.eventuate.javaclient.spring.common.common;

import io.eventuate.javaclient.commonimpl.common.schema.ConfigurableEventSchema;
import io.eventuate.javaclient.commonimpl.common.schema.DefaultEventuateEventSchemaManager;
import io.eventuate.javaclient.commonimpl.common.schema.EventSchemaConfigurer;
import io.eventuate.javaclient.commonimpl.common.schema.EventuateEventSchemaManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class EventuateCommonCommonConfiguration {

  @Autowired(required=false)
  private EventSchemaConfigurer[] metadataManagerConfigurers = new EventSchemaConfigurer[0];

  @Bean
  public EventuateEventSchemaManager eventSchemaMetadataManager() {
    DefaultEventuateEventSchemaManager eventSchemaManager = new DefaultEventuateEventSchemaManager();
    ConfigurableEventSchema configuration = new ConfigurableEventSchema(eventSchemaManager);
    Arrays.stream(metadataManagerConfigurers).forEach(c -> c.configure(configuration));
    return eventSchemaManager;
  }
}
