package io.eventuate.local.cdc.main;

import io.eventuate.local.cdc.debezium.AbstractEventTableChangesToAggregateTopicRelay;
import io.eventuate.local.cdc.debezium.RelayStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class RelayHealthIndicator implements HealthIndicator {
  private AbstractEventTableChangesToAggregateTopicRelay relay;

  public RelayHealthIndicator(AbstractEventTableChangesToAggregateTopicRelay relay) {

    this.relay = relay;
  }

  @Override
  public Health health() {
    RelayStatus status = relay.getStatus();
    switch (status) {
      case FAILED:
        return Health.down().build();
      default:
        return Health.up().withDetail("status", status.name()).build();
    }
  }
}
