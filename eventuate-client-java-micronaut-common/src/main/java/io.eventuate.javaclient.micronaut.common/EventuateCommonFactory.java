package io.eventuate.javaclient.micronaut.common;

import io.eventuate.*;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.AggregateEvents;
import io.eventuate.javaclient.commonimpl.EventuateAggregateStoreImpl;
import io.eventuate.javaclient.commonimpl.SerializedEventDeserializer;
import io.eventuate.javaclient.commonimpl.schema.ConfigurableEventSchema;
import io.eventuate.javaclient.commonimpl.schema.DefaultEventuateEventSchemaManager;
import io.eventuate.javaclient.commonimpl.schema.EventSchemaConfigurer;
import io.eventuate.javaclient.commonimpl.schema.EventuateEventSchemaManager;
import io.micronaut.context.annotation.Factory;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.Arrays;

@Factory
public class EventuateCommonFactory {

  @Singleton
  public EventuateEventSchemaManager eventSchemaMetadataManager(EventSchemaConfigurer[] metadataManagerConfigurers) {
    DefaultEventuateEventSchemaManager eventSchemaManager = new DefaultEventuateEventSchemaManager();
    ConfigurableEventSchema configuration = new ConfigurableEventSchema(eventSchemaManager);
    Arrays.stream(metadataManagerConfigurers).forEach(c -> c.configure(configuration));
    return eventSchemaManager;
  }

  @Singleton
  public EventuateAggregateStore aggregateEventStore(MissingApplyEventMethodStrategy[] missingApplyEventMethodStrategies,
                                                         @Nullable SerializedEventDeserializer serializedEventDeserializer,
                                                         AggregateCrud restClient,
                                                         AggregateEvents stompClient,
                                                         SnapshotManager snapshotManager,
                                                         EventuateEventSchemaManager eventuateEventSchemaManager) {
    EventuateAggregateStoreImpl eventuateAggregateStore = new EventuateAggregateStoreImpl(restClient,
            stompClient,
            snapshotManager,
            new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies),
            eventuateEventSchemaManager
    );

    if (serializedEventDeserializer != null)
      eventuateAggregateStore.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStore;
  }

  @Singleton
  public io.eventuate.sync.EventuateAggregateStore syncAggregateEventStore(MissingApplyEventMethodStrategy[] missingApplyEventMethodStrategies,
                                                               @Nullable SerializedEventDeserializer serializedEventDeserializer,
                                                               io.eventuate.javaclient.commonimpl.sync.AggregateCrud restClient,
                                                               io.eventuate.javaclient.commonimpl.sync.AggregateEvents stompClient,
                                                               SnapshotManager snapshotManager) {
    io.eventuate.javaclient.commonimpl.sync.EventuateAggregateStoreImpl eventuateAggregateStore =
            new io.eventuate.javaclient.commonimpl.sync.EventuateAggregateStoreImpl(restClient,
                    stompClient,
                    snapshotManager,
                    new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies));

    if (serializedEventDeserializer != null)
      eventuateAggregateStore.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStore;
  }



  @Singleton
  public SnapshotManager snapshotManager(SnapshotStrategy[] snapshotStrategies) {
    SnapshotManagerImpl snapshotManager = new SnapshotManagerImpl();
    for (SnapshotStrategy ss : snapshotStrategies)
      snapshotManager.addStrategy(ss);
    return snapshotManager;
  }

  @Singleton
  public CompositeMissingApplyEventMethodStrategy compositeMissingApplyEventMethodStrategy(MissingApplyEventMethodStrategy[] missingApplyEventMethodStrategies) {
    return new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies);
  }
}
