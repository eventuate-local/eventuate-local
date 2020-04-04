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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateCommonConfiguration {

  @Autowired(required=false)
  private SerializedEventDeserializer serializedEventDeserializer;

  @Autowired(required=false)
  private MissingApplyEventMethodStrategy[] missingApplyEventMethodStrategies = new MissingApplyEventMethodStrategy[0];

  @Bean
  public EventuateAggregateStore aggregateEventStore(AggregateCrud restClient, AggregateEvents stompClient, SnapshotManager snapshotManager) {
    EventuateAggregateStoreImpl eventuateAggregateStore = new EventuateAggregateStoreImpl(restClient, stompClient, snapshotManager, new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies));

    if (serializedEventDeserializer != null)
      eventuateAggregateStore.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStore;
  }


  @Bean
  public io.eventuate.sync.EventuateAggregateStore syncAggregateEventStore(io.eventuate.javaclient.commonimpl.sync.AggregateCrud restClient,
                                                                           io.eventuate.javaclient.commonimpl.sync.AggregateEvents stompClient, SnapshotManager snapshotManager) {
    io.eventuate.javaclient.commonimpl.sync.EventuateAggregateStoreImpl eventuateAggregateStore =
            new io.eventuate.javaclient.commonimpl.sync.EventuateAggregateStoreImpl(restClient, stompClient, snapshotManager, new CompositeMissingApplyEventMethodStrategy(missingApplyEventMethodStrategies));

    if (serializedEventDeserializer != null)
      eventuateAggregateStore.setSerializedEventDeserializer(serializedEventDeserializer);

    return eventuateAggregateStore;
  }

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
}
