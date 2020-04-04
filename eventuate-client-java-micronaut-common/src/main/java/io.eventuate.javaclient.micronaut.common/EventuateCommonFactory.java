package io.eventuate.javaclient.micronaut.common;

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
import io.micronaut.context.annotation.Factory;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Factory
public class EventuateCommonFactory {

  @Singleton
  public EventuateAggregateStore aggregateEventStore(MissingApplyEventMethodStrategy[] missingApplyEventMethodStrategies,
                                                     @Nullable SerializedEventDeserializer serializedEventDeserializer,
                                                     AggregateCrud restClient,
                                                     AggregateEvents stompClient,
                                                     SnapshotManager snapshotManager) {
    EventuateAggregateStoreImpl eventuateAggregateStore = new EventuateAggregateStoreImpl(restClient,
            stompClient,
            snapshotManager,
            new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies));

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
