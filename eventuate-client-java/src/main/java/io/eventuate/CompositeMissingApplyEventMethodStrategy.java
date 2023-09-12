package io.eventuate;

import java.util.Arrays;

public class CompositeMissingApplyEventMethodStrategy {
  private MissingApplyEventMethodStrategy[] strategies;
  private DefaultMissingApplyEventMethodStrategy defaultStrategy = new DefaultMissingApplyEventMethodStrategy();

  public CompositeMissingApplyEventMethodStrategy(MissingApplyEventMethodStrategy[] strategies) {
    this.strategies = strategies;
  }

  public boolean supports(Aggregate aggregate, MissingApplyMethodException e) {
    return Arrays.stream(strategies).anyMatch(s -> s.supports(aggregate, e));
  }


  public void handle(Aggregate aggregate, MissingApplyMethodException e) {
    for (MissingApplyEventMethodStrategy strategy : strategies) {
      if (strategy.supports(aggregate, e)) {
        strategy.handle(aggregate, e);
        return;
      }
    }
    defaultStrategy.handle(aggregate, e);
  }

  public MissingApplyEventMethodStrategy toMissingApplyEventMethodStrategy() {
    return new MissingApplyEventMethodStrategy() {
      @Override
      public boolean supports(Aggregate aggregate, MissingApplyMethodException e) {
        return CompositeMissingApplyEventMethodStrategy.this.supports(aggregate, e);
      }

      @Override
      public void handle(Aggregate aggregate, MissingApplyMethodException e) {
        CompositeMissingApplyEventMethodStrategy.this.handle(aggregate, e);
      }
    };
  }
}
