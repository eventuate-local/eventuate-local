package io.eventuate.javaclient.micronaut.common.crud;

import io.eventuate.AggregateRepository;
import io.eventuate.CompositeMissingApplyEventMethodStrategy;
import io.micronaut.context.annotation.Context;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@Context
public class AggregateRepositoryInitializer {

  private CompositeMissingApplyEventMethodStrategy strategies;
  private AggregateRepository[] aggregateRepositories;
  private io.eventuate.sync.AggregateRepository[] syncAggregateRepositories;

  public AggregateRepositoryInitializer(CompositeMissingApplyEventMethodStrategy strategies, AggregateRepository[] aggregateRepositories, io.eventuate.sync.AggregateRepository[] syncAggregateRepositories) {
    this.strategies = strategies;
    this.aggregateRepositories = aggregateRepositories;
    this.syncAggregateRepositories = syncAggregateRepositories;
  }

  @PostConstruct
  public void setMissingStrategies() {
    for (AggregateRepository aggregateRepository : aggregateRepositories) {
      aggregateRepository.setMissingApplyEventMethodStrategy(strategies.toMissingApplyEventMethodStrategy());
    }

    for (io.eventuate.sync.AggregateRepository aggregateRepository : syncAggregateRepositories) {
      aggregateRepository.setMissingApplyEventMethodStrategy(strategies.toMissingApplyEventMethodStrategy());

    }
  }
}
