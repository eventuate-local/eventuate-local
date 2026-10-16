package io.eventuate.javaclient.spring.tests.common;

import io.eventuate.Aggregate;
import io.eventuate.EntityIdAndVersion;
import io.eventuate.EntityWithMetadata;
import io.eventuate.Event;
import io.eventuate.SaveOptions;
import io.eventuate.sync.EventuateAggregateStoreCrud;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class AbstractSpringAccountIntegrationSyncTest extends AbstractSpringAccountIntegrationTest {

  @Autowired
  private EventuateAggregateStoreCrud aggregateStore;

  @Override
  protected <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> classz, List<Event> events, Optional<SaveOptions> saveOptions) {
    return aggregateStore.save(classz, events, saveOptions);
  }

  @Override
  protected <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId) {
    return aggregateStore.find(clasz, entityId);
  }
}
