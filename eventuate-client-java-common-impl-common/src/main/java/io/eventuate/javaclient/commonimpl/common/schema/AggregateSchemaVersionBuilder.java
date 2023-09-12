package io.eventuate.javaclient.commonimpl.common.schema;

import java.util.ArrayList;
import java.util.List;

public class AggregateSchemaVersionBuilder {

  private final AggregateSchemaBuilder aggregateSchemaBuilder;
  private final String version;
  private List<EventRename> renames = new ArrayList<>();
  private List<EventTransform> transforms = new ArrayList<>();

  public AggregateSchemaVersionBuilder(AggregateSchemaBuilder aggregateSchemaBuilder, String version) {

    this.aggregateSchemaBuilder = aggregateSchemaBuilder;
    this.version = version;
  }

  public AggregateSchemaVersionBuilder rename(String oldEventName, String newEventName) {
    this.renames.add(new EventRename(oldEventName, newEventName));
    return this;
  }

  public AggregateSchemaVersionBuilder transform(String eventName, EventUpcaster upcaster) {
    this.transforms.add(new EventTransform(eventName, upcaster));
    return this;
  }

  public AggregateSchemaVersionBuilder version(String version) {
    finishForAggregate();
    return aggregateSchemaBuilder.version(version);
  }

  private void finishForAggregate() {
    aggregateSchemaBuilder.addVersion(version, renames, transforms);
  }

  public void customize() {
    finishForAggregate();
    aggregateSchemaBuilder.build();
  }

  public AggregateSchemaBuilder forAggregate(String account) {
    finishForAggregate();
    return aggregateSchemaBuilder.forAggregate(account);
  }
}
