package io.eventuate.javaclient.commonimpl.schema;

import java.util.ArrayList;
import java.util.List;

public class AggregateSchemaBuilder {

  private final ConfigurableEventSchema configurableEventSchema;
  private final String aggregateType;
  private List<AggregateSchemaVersion> versions = new ArrayList<>();

  public AggregateSchemaBuilder(ConfigurableEventSchema configuration, String aggregateType) {
    this.configurableEventSchema = configuration;
    this.aggregateType = aggregateType;
  }

  public AggregateSchemaVersionBuilder version(String version) {
    return new AggregateSchemaVersionBuilder(this, version);
  }

  protected void addVersion(String version, List<EventRename> renames, List<EventTransform> transforms) {
    this.versions.add(new AggregateSchemaVersion(version, renames, transforms));
  }

  public AggregateSchemaBuilder forAggregate(String aggregateType) {
    return configurableEventSchema.finishForAggregateAndStartNew(this.aggregateType, versions, aggregateType);
  }

  protected void build() {
    configurableEventSchema.finishForAggregateAndBuild(this.aggregateType, versions);
  }
}
