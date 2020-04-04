package io.eventuate;

import java.util.Arrays;

public class CompositeMissingApplyEventMethodStrategy implements MissingApplyEventMethodStrategy {
  private MissingApplyEventMethodStrategy[] strategies;
  private DefaultMissingApplyEventMethodStrategy defaultStrategy = new DefaultMissingApplyEventMethodStrategy();

  public CompositeMissingApplyEventMethodStrategy(MissingApplyEventMethodStrategy[] strategies) {
    this.strategies = strategies;
  }

  @Override
  public boolean supports(Aggregate aggregate, MissingApplyMethodException e) {
    return Arrays.stream(strategies).anyMatch(s -> s.supports(aggregate, e));
  }

  @Override
  public void handle(Aggregate aggregate, MissingApplyMethodException e) {
    for (MissingApplyEventMethodStrategy strategy : strategies) {
      if (strategy.supports(aggregate, e)) {
        strategy.handle(aggregate, e);
        return;
      }
    }
    defaultStrategy.handle(aggregate, e);
  }
}
