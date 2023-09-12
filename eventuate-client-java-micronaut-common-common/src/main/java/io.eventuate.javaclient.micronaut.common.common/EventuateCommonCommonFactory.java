package io.eventuate.javaclient.micronaut.common.common;

import io.eventuate.javaclient.commonimpl.common.schema.ConfigurableEventSchema;
import io.eventuate.javaclient.commonimpl.common.schema.DefaultEventuateEventSchemaManager;
import io.eventuate.javaclient.commonimpl.common.schema.EventSchemaConfigurer;
import io.eventuate.javaclient.commonimpl.common.schema.EventuateEventSchemaManager;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;
import java.util.Arrays;

@Factory
public class EventuateCommonCommonFactory {

  @Singleton
  public EventuateEventSchemaManager eventSchemaMetadataManager(EventSchemaConfigurer[] metadataManagerConfigurers) {
    DefaultEventuateEventSchemaManager eventSchemaManager = new DefaultEventuateEventSchemaManager();
    ConfigurableEventSchema configuration = new ConfigurableEventSchema(eventSchemaManager);
    Arrays.stream(metadataManagerConfigurers).forEach(c -> c.configure(configuration));
    return eventSchemaManager;
  }
}
