package io.eventuate.javaclient.micronaut.tests.common;

import io.eventuate.*;
import io.eventuate.sync.EventuateAggregateStore;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class AbstractMicronautAccountIntegrationSyncTest extends AbstractMicronautAccountIntegrationTest {

  @Inject
  private EventuateAggregateStore aggregateStore;

  @Override
  protected <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> classz, List<Event> events, Optional<SaveOptions> saveOptions) {
    return aggregateStore.save(classz, events, saveOptions);
  }

  @Override
  protected <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId) {
    return aggregateStore.find(clasz, entityId);
  }
}
