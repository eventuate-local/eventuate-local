package io.eventuate;

public class DefaultMissingApplyEventMethodStrategy implements MissingApplyEventMethodStrategy {

  @Override
  public boolean supports(Aggregate aggregate, MissingApplyMethodException e) {
    return true;
  }

  @Override
  public void handle(Aggregate aggregate, MissingApplyMethodException e) {
    throw e;
  }

  public static MissingApplyEventMethodStrategy INSTANCE = new DefaultMissingApplyEventMethodStrategy();
}
