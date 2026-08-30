package io.eventuate.javaclient.spring.tests.common;

import io.eventuate.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static io.eventuate.testutil.AsyncUtil.await;

public class AbstractSpringAccountIntegrationReactiveTest extends AbstractSpringAccountIntegrationTest {

  @Autowired
  private EventuateAggregateStoreCrud aggregateStore;

  @Override
  protected <T extends Aggregate<T>> EntityIdAndVersion save(Class<T> classz, List<Event> events, Optional<SaveOptions> saveOptions) {
    return await(aggregateStore.save(classz, events, saveOptions));
  }

  @Override
  protected <T extends Aggregate<T>> EntityWithMetadata<T> find(Class<T> clasz, String entityId) {
    return await(aggregateStore.find(clasz, entityId));
  }
}
