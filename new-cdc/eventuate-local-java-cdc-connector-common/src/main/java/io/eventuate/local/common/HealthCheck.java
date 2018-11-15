package io.eventuate.local.common;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class HealthCheck implements HealthIndicator {

  private final Object monitor = new Object();

  private Set<HealthComponent> healthComponents = new HashSet<>();

  public HealthComponent getHealthComponent() {
    synchronized (monitor) {
      HealthComponent healthComponent = new HealthComponent();
      healthComponents.add(healthComponent);
      return healthComponent;
    }
  }

  public void returnHealthComponent(HealthComponent healthComponent) {
    synchronized (monitor) {
      healthComponents.remove(healthComponent);
    }
  }

  @Override
  public Health health() {
    List<String> messages;
    synchronized (monitor) {
      messages = healthComponents
              .stream()
              .filter(healthComponent -> !healthComponent.healthy)
              .map(healthComponent -> healthComponent.message)
              .collect(Collectors.toList());
    }


    if (!messages.isEmpty()) {
      Health.Builder builder = Health.down();

      for (int i = 1; i <= messages.size(); i++) {
        builder.withDetail("error-" + i, messages.get(i - 1));
      }

      return builder.build();
    }

    return Health.up().build();
  }

  public class HealthComponent {
    private UUID id = UUID.randomUUID();

    private volatile boolean healthy = true;
    private String message;

    private HealthComponent() {
    }

    public void markAsHealthy() {
      healthy = true;
    }

    public void markAsUnhealthy(String reason) {
      synchronized (monitor) {
        healthy = false;
        message = reason;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      HealthComponent that = (HealthComponent) o;
      return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }
  }
}
