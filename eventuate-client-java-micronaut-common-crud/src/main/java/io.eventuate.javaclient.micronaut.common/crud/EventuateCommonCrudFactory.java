package io.eventuate.javaclient.micronaut.common.crud;

import io.eventuate.*;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrud;
import io.eventuate.javaclient.commonimpl.crud.EventuateAggregateStoreCrudImpl;
import io.eventuate.javaclient.commonimpl.common.schema.EventuateEventSchemaManager;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class EventuateCommonCrudFactory {

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

  @Singleton
  public EventuateAggregateStoreCrud eventuateAggregateStoreCrud(MissingApplyEventMethodStrategy[] missingApplyEventMethodStrategies,
                                                         AggregateCrud aggregateCrud,
                                                         SnapshotManager snapshotManager,
                                                         EventuateEventSchemaManager eventuateEventSchemaManager) {
    EventuateAggregateStoreCrud eventuateAggregateStoreCrud = new EventuateAggregateStoreCrudImpl(aggregateCrud,
            snapshotManager,
            new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies),
            eventuateEventSchemaManager
    );

    return eventuateAggregateStoreCrud;
  }

  @Singleton
  public io.eventuate.sync.EventuateAggregateStoreCrud syncEventuateAggregateStoreCrud(MissingApplyEventMethodStrategy[] missingApplyEventMethodStrategies,
                                                                           io.eventuate.javaclient.commonimpl.crud.sync.AggregateCrud aggregateCrud,
                                                                           SnapshotManager snapshotManager) {

    io.eventuate.sync.EventuateAggregateStoreCrud eventuateAggregateStoreCrud =
            new io.eventuate.javaclient.commonimpl.crud.sync.EventuateAggregateStoreCrudImpl(aggregateCrud,
            snapshotManager,
            new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies));

    return eventuateAggregateStoreCrud;
  }
}
