package io.eventuate.local.unified.cdc.pipeline.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.List;

public abstract class AbstractHealthCheck implements HealthIndicator {
  protected Health checkErrors(List<String> errorMessages) {
    if (!errorMessages.isEmpty()) {
      Health.Builder builder = Health.down();

      for (int i = 1; i <= errorMessages.size(); i++) {
        builder.withDetail("error-" + i, errorMessages.get(i - 1));
      }

      return builder.build();
    }

    return Health.up().build();
  }
}
