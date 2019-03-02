package io.eventuate.local.unified.cdc.pipeline.common.health;

import org.springframework.boot.actuate.health.Health;

import java.util.LinkedList;
import java.util.List;

public class HealthBuilder {

  private List<String> errors = new LinkedList<>();
  private List<String> details = new LinkedList<>();;

  final public Health build() {
    Health.Builder builder = errors.isEmpty() ? Health.up() : Health.down();

    for (int i = 1; i <= errors.size(); i++) {
      builder.withDetail("error-" + i, errors.get(i - 1));
    }

    for (int i = 1; i <= details.size(); i++) {
      builder.withDetail("detail-" + i, details.get(i - 1));
    }

    return builder.build();
  }

  public void addError(String error) {
    errors.add(error);
  }

  public void addDetail(String detail) {
    details.add(detail);
  }
}
