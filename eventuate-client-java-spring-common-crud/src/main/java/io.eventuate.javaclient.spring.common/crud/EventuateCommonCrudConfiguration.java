package io.eventuate.javaclient.spring.common.crud;

import io.eventuate.*;
import io.eventuate.javaclient.commonimpl.crud.AggregateCrud;
import io.eventuate.javaclient.commonimpl.crud.EventuateAggregateStoreCrudImpl;
import io.eventuate.javaclient.commonimpl.common.schema.EventuateEventSchemaManager;
import io.eventuate.javaclient.spring.common.common.EventuateCommonCommonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateCommonCommonConfiguration.class)
public class EventuateCommonCrudConfiguration {

  @Autowired(required=false)
  private MissingApplyEventMethodStrategy[] missingApplyEventMethodStrategies = new MissingApplyEventMethodStrategy[0];


  @Autowired(required=false)
  private SnapshotStrategy[] snapshotStrategies = new SnapshotStrategy[0];


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

  @Bean
  public EventuateAggregateStoreCrud eventuateAggregateStoreCrud(AggregateCrud aggregateCrud,
                                                                 SnapshotManager snapshotManager,
                                                                 EventuateEventSchemaManager eventuateEventSchemaManager) {
    EventuateAggregateStoreCrud eventuateAggregateStoreCrud = new EventuateAggregateStoreCrudImpl(aggregateCrud,
            snapshotManager,
            new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies).toMissingApplyEventMethodStrategy(),
            eventuateEventSchemaManager
    );

    return eventuateAggregateStoreCrud;
  }

  @Bean
  public io.eventuate.sync.EventuateAggregateStoreCrud syncEventuateAggregateStoreCrud(io.eventuate.javaclient.commonimpl.crud.sync.AggregateCrud aggregateCrud,
                                                                                       SnapshotManager snapshotManager) {

    io.eventuate.sync.EventuateAggregateStoreCrud eventuateAggregateStoreCrud =
            new io.eventuate.javaclient.commonimpl.crud.sync.EventuateAggregateStoreCrudImpl(aggregateCrud,
                    snapshotManager,
                    new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies).toMissingApplyEventMethodStrategy());

    return eventuateAggregateStoreCrud;
  }
}
