package io.eventuate.javaclient.micronaut.tests.common;

import io.eventuate.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static io.eventuate.testutil.AsyncUtil.await;

public class AbstractMicronautAccountIntegrationReactiveTest extends AbstractMicronautAccountIntegrationTest {

  @Inject
  private EventuateAggregateStore aggregateStore;

  @Override
  protected <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> classz, List<Event> events, Optional<SaveOptions> saveOptions) {
    return await(aggregateStore.save(classz, events, saveOptions));
  }

  @Override
  protected <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId) {
    return await(aggregateStore.find(clasz, entityId));
  }
}