package io.eventuate.javaclient.commonimpl.schema;

import java.util.List;

public class ConfigurableEventSchema {
  private DefaultEventuateEventSchemaManager eventSchemaManager;

  public ConfigurableEventSchema(DefaultEventuateEventSchemaManager eventSchemaManager) {
    this.eventSchemaManager = eventSchemaManager;
  }


  public AggregateSchemaBuilder forAggregate(String aggregateType) {
    return new AggregateSchemaBuilder(this, aggregateType);
  }

  /**
   * @param aggregateType - the fully qualified class name
   * @param externalAggregateType - the external name, e.g. Kafka topic - TODO implement <=> Mapping
   * @return a builder
   */
  public AggregateSchemaBuilder forAggregate(String aggregateType, String externalAggregateType) {
    return new AggregateSchemaBuilder(this, aggregateType);
  }

  protected AggregateSchemaBuilder finishForAggregateAndStartNew(String aggregateType, List<AggregateSchemaVersion> versions, String nextAggregateType) {
    finishForAggregate(aggregateType, versions);
    return new AggregateSchemaBuilder(this, nextAggregateType);
  }

  private void finishForAggregate(String aggregateType, List<AggregateSchemaVersion> versions) {
    this.eventSchemaManager.add(new AggregateSchema(aggregateType, versions));
  }

  protected void finishForAggregateAndBuild(String aggregateType, List<AggregateSchemaVersion> versions) {
    finishForAggregate(aggregateType, versions);
  }

  public AggregateSchemaBuilder forAggregate(Class clasz) {
    return forAggregate(clasz.getName());
  }
}
