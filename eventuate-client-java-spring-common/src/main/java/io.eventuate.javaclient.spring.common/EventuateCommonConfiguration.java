package io.eventuate.javaclient.spring.common;

import io.eventuate.CompositeMissingApplyEventMethodStrategy;
import io.eventuate.EventuateAggregateStore;
import io.eventuate.MissingApplyEventMethodStrategy;
import io.eventuate.SnapshotManager;
import io.eventuate.SnapshotManagerImpl;
import io.eventuate.SnapshotStrategy;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.AggregateEvents;
import io.eventuate.javaclient.commonimpl.EventuateAggregateStoreImpl;
import io.eventuate.javaclient.commonimpl.SerializedEventDeserializer;
import io.eventuate.javaclient.commonimpl.schema.DefaultEventuateEventSchemaManager;
import io.eventuate.javaclient.commonimpl.schema.ConfigurableEventSchema;
import io.eventuate.javaclient.commonimpl.schema.EventSchemaConfigurer;
import io.eventuate.javaclient.commonimpl.schema.EventuateEventSchemaManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class EventuateCommonConfiguration {

  @Autowired(required=false)
  private SerializedEventDeserializer serializedEventDeserializer;

  @Autowired(required=false)
  private MissingApplyEventMethodStrategy[] missingApplyEventMethodStrategies = new MissingApplyEventMethodStrategy[0];


  @Autowired(required=false)
  private SnapshotStrategy[] snapshotStrategies = new SnapshotStrategy[0];

  @Autowired(required=false)
  private EventSchemaConfigurer[] metadataManagerConfigurers = new EventSchemaConfigurer[0];

  @Bean
  public EventuateEventSchemaManager eventSchemaMetadataManager() {
    DefaultEventuateEventSchemaManager eventSchemaManager = new DefaultEventuateEventSchemaManager();
    ConfigurableEventSchema configuration = new ConfigurableEventSchema(eventSchemaManager);
    Arrays.stream(metadataManagerConfigurers).forEach(c -> c.configure(configuration));
    return eventSchemaManager;
  }

  @Bean
  public EventuateAggregateStore aggregateEventStore(AggregateCrud aggregateCrud, AggregateEvents aggregateEvents, SnapshotManager snapshotManager, EventuateEventSchemaManager eventuateEventSchemaManager) {
    EventuateAggregateStoreImpl eventuateAggregateStore = new EventuateAggregateStoreImpl(aggregateCrud, aggregateEvents, snapshotManager,
            new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies), eventuateEventSchemaManager);

    if (serializedEventDeserializer != null)
      eventuateAggregateStore.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStore;
  }


  @Bean
  public io.eventuate.sync.EventuateAggregateStore syncAggregateEventStore(io.eventuate.javaclient.commonimpl.sync.AggregateCrud aggregateCrud,
                                                                           io.eventuate.javaclient.commonimpl.sync.AggregateEvents aggregateEvents, SnapshotManager snapshotManager) {
    io.eventuate.javaclient.commonimpl.sync.EventuateAggregateStoreImpl eventuateAggregateStore =
            new io.eventuate.javaclient.commonimpl.sync.EventuateAggregateStoreImpl(aggregateCrud, aggregateEvents, snapshotManager, new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies));

    if (serializedEventDeserializer != null)
      eventuateAggregateStore.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStore;
  }

  @Bean
  public SnapshotManager snapshotManager() {
    SnapshotManagerImpl snapshotManager = new SnapshotManagerImpl();
    for (SnapshotStrategy ss : snapshotStrategies)
      snapshotManager.addStrategy(ss);
    return snapshotManager;
  }

  @Bean
  public AggregateRepositoryBeanPostProcessor aggregateRepositoryBeanPostProcessor() {
    return new AggregateRepositoryBeanPostProcessor(new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies));
  }
}
